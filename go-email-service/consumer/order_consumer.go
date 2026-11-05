package consumer

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/joho/godotenv"
	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/sanglam2806/go-email-service/model"
)

const (
	maxRetries       = 3
	retryCountHeader = "x-retry-count"
	// changing this needs the retry queue deleted first: RabbitMQ rejects a redeclare with different arguments
	retryDelayMs int32 = 10000

	reconnectDelay = 5 * time.Second
	confirmTimeout = 5 * time.Second
	// pause before requeueing after a failed move, so a broken queue doesn't cause a tight redelivery loop
	requeueDelay = 1 * time.Second
)

// publisher publishes with broker confirmation on a channel in confirm mode.
type publisher struct {
	ch      *amqp.Channel
	returns chan amqp.Return
}

// StartConsuming runs the consumer forever, reconnecting whenever the connection or channel is lost.
// Unacked messages stay in RabbitMQ and are redelivered after reconnecting.
func StartConsuming() {
	if _, err := os.Stat(".env"); err == nil {
		_ = godotenv.Load()
	}

	mq_url := LoadEnv("MQ_SERVER")
	queue := LoadEnv("ORDER_QUEUE")

	for {
		err := consume(mq_url, queue)
		log.Printf("Consumer stopped, reconnecting in %s: %s", reconnectDelay, err)
		time.Sleep(reconnectDelay)
	}
}

// consume connects, declares the queues and processes messages until the connection or channel is lost.
// It always returns a non-nil error describing why it stopped.
func consume(mq_url string, queue string) error {
	conn, err := ConnectToMq(mq_url)
	if err != nil {
		return fmt.Errorf("connect to MQ server: %w", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		return fmt.Errorf("open channel: %w", err)
	}
	defer ch.Close()

	closed := ch.NotifyClose(make(chan *amqp.Error, 1))

	// Declare the queue, startup order doesn't matter: the Java service only declares it
	// on its first publish. Settings must match RabbitMQConfig.queue() or RabbitMQ rejects the declare.
	_, err = ch.QueueDeclare(
		queue,
		true,  // durable
		false, // auto-delete
		false, // exclusive
		false, // no-wait
		nil,   // args
	)
	if err != nil {
		return fmt.Errorf("declare queue %s: %w", queue, err)
	}

	retryQueue := queue + ".retry"
	dlq := queue + ".dlq"

	// The retry queue has no consumer: a message waits here for the TTL, then RabbitMQ
	// dead-letters it through the default exchange back to the main queue.
	_, err = ch.QueueDeclare(
		retryQueue,
		true,  // durable
		false, // auto-delete
		false, // exclusive
		false, // no-wait
		amqp.Table{
			"x-message-ttl":             retryDelayMs,
			"x-dead-letter-exchange":    "",
			"x-dead-letter-routing-key": queue,
		},
	)
	if err != nil {
		return fmt.Errorf("declare queue %s: %w", retryQueue, err)
	}

	// Messages that can't be processed are parked here for inspection and replay
	_, err = ch.QueueDeclare(
		dlq,
		true,  // durable
		false, // auto-delete
		false, // exclusive
		false, // no-wait
		nil,   // args
	)
	if err != nil {
		return fmt.Errorf("declare queue %s: %w", dlq, err)
	}

	// Confirm mode: the broker acks each publish, so moveTo knows the copy is safe before acking the original
	if err := ch.Confirm(false); err != nil {
		return fmt.Errorf("enable publisher confirms: %w", err)
	}
	// Buffered because moveTo publishes one message at a time and reads the return afterwards
	pub := publisher{ch: ch, returns: ch.NotifyReturn(make(chan amqp.Return, 1))}

	// prefetch: RabbitMQ sends at most 10 messages that are not yet acked
	if err := ch.Qos(10, 0, false); err != nil {
		return fmt.Errorf("set QoS: %w", err)
	}

	msgs, err := ch.Consume(
		queue, // queue name - must match Java config
		"",    // consumer tag
		false, // auto-acknowledge
		false, // exclusive
		false, // no-local
		false, // no-wait
		nil,   // args
	)
	if err != nil {
		return fmt.Errorf("consume queue %s: %w", queue, err)
	}

	// With manual ack every message must end in exactly one Ack, Nack or moveTo, otherwise it
	// stays unacked, holds a prefetch slot and is redelivered when the consumer restarts.
	for msg := range msgs {
		var order model.Order
		if err := json.Unmarshal(msg.Body, &order); err != nil {
			// retrying can't fix bad JSON, so skip the retry queue
			log.Printf("Cannot parse message body %q, moving to %s: %s", msg.Body, dlq, err)
			moveTo(pub, dlq, msg, retryCount(msg))
			continue
		}

		if err := sendMail(order); err != nil {
			count := retryCount(msg)
			if count < maxRetries {
				log.Printf("Cannot send email for order %d, retry %d/%d in %d ms: %s",
					order.OrderId, count+1, maxRetries, retryDelayMs, err)
				moveTo(pub, retryQueue, msg, count+1)
			} else {
				log.Printf("Cannot send email for order %d after %d retries, moving to %s: %s",
					order.OrderId, count, dlq, err)
				moveTo(pub, dlq, msg, count)
			}
			continue
		}

		if err := msg.Ack(false); err != nil {
			log.Printf("Cannot ack order %d: %s", order.OrderId, err)
		}
	}

	// The delivery channel closed. Closing our side also ends a consumer cancelled by the broker,
	// for example when the queue was deleted, and guarantees the read below can't block.
	_ = ch.Close()
	if closeErr := <-closed; closeErr != nil {
		return closeErr
	}
	return errors.New("consumer was cancelled by the broker")
}

// moveTo publishes a copy of msg to targetQueue and acks the original once the broker has confirmed
// the copy. If that fails the original is requeued, so a message is never lost; a crash or a late
// confirm can deliver it twice, so processing must be idempotent.
func moveTo(pub publisher, targetQueue string, msg amqp.Delivery, count int) {
	if err := pub.publishConfirmed(targetQueue, msg, count); err != nil {
		log.Printf("Cannot move message to %s, requeueing: %s", targetQueue, err)
		time.Sleep(requeueDelay)
		if err := msg.Nack(false, true); err != nil {
			log.Printf("Cannot nack message %d: %s", msg.DeliveryTag, err)
		}
		return
	}

	if err := msg.Ack(false); err != nil {
		log.Printf("Cannot ack message %d: %s", msg.DeliveryTag, err)
	}
}

// publishConfirmed returns nil only if the broker confirmed the message and routed it to a queue.
func (p publisher) publishConfirmed(targetQueue string, msg amqp.Delivery, count int) error {
	ctx, cancel := context.WithTimeout(context.Background(), confirmTimeout)
	defer cancel()

	confirm, err := p.ch.PublishWithDeferredConfirmWithContext(
		ctx,
		"",          // default exchange: routes straight to the queue named by the routing key
		targetQueue, // routing key
		true,        // mandatory: return the message if no queue has that name
		false,       // immediate
		amqp.Publishing{
			ContentType:  msg.ContentType,
			DeliveryMode: amqp.Persistent,
			Headers:      amqp.Table{retryCountHeader: int32(count)},
			Body:         msg.Body,
		},
	)
	if err != nil {
		return err
	}

	acked, err := confirm.WaitContext(ctx)
	if err != nil {
		return fmt.Errorf("no broker confirm within %s: %w", confirmTimeout, err)
	}
	if !acked {
		return errors.New("broker rejected the message or the channel closed")
	}

	// An unroutable message is confirmed too, but RabbitMQ sends the return before the ack,
	// so it is already in the buffer here.
	select {
	case returned, ok := <-p.returns:
		if ok {
			return fmt.Errorf("not routed to a queue: %s", returned.ReplyText)
		}
	default:
	}
	return nil
}

// retryCount reads how many times a message has been retried. AMQP integers can arrive as
// different Go types, and a message that hasn't been retried has no header at all.
func retryCount(msg amqp.Delivery) int {
	switch v := msg.Headers[retryCountHeader].(type) {
	case int32:
		return int(v)
	case int64:
		return int(v)
	}
	return 0
}

func ConnectToMq(url string) (*amqp.Connection, error) {
	var conn *amqp.Connection
	var err error

	for i := 0; i < 10; i++ {
		conn, err = amqp.Dial(url)
		if err == nil {
			fmt.Println("Connected to MQ server")
			return conn, nil
		}
		fmt.Printf("MQServer is not ready! Retry after 3s... %d time\n", i+1)
		time.Sleep(3 * time.Second)
	}

	return nil, err
}

func LoadEnv(key string) string {
	val, exist := os.LookupEnv(key)

	if !exist {
		log.Fatalf("key %s does not exist", key)
	}
	return val
}

// sendMail is a stand-in for real email delivery; note "fail-email" simulates an SMTP failure.
func sendMail(order model.Order) error {
	if order.Note == "fail-email" {
		return errors.New("smtp timeout")
	}
	fmt.Printf("order reveiced: %+v\n", order)
	return nil
}
