package consumer

import (
	"encoding/json"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/joho/godotenv"
	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/sanglam2806/go-email-service/model"
)

func StartConsuming() {
	if _, err := os.Stat(".env"); err == nil {
		_ = godotenv.Load()
	}

	mq_url := LoadEnv("MQ_SERVER")
	queue := LoadEnv("ORDER_QUEUE")

	conn, err := ConnectToMq(mq_url)
	FailOnError(err, "Fail to connect to MQ Server")
	defer conn.Close()

	ch, err := conn.Channel()
	FailOnError(err, "Fail to connect to Channel")
	defer ch.Close()

	msgs, err := ch.Consume(
		queue,  // queue name - must match Java config
		"",             // consumer tag
		true,           // auto-acknowledge
		false,          // exclusive
		false,          // no-local
		false,          // no-wait
		nil,            // args
		)
	FailOnError(err, "Cannot get Messages")

	for msg := range msgs {
		var order model.Order
		err = json.Unmarshal(msg.Body, &order)
		FailOnError(err, "Fail to parse body!")

		if err != nil {
			log.Panic("Can not parse Queue Body")
			continue
		}
		fmt.Printf("order reveiced: %+v\n", order)	
	}
}

func ConnectToMq(url string) (*amqp.Connection, error){
	var conn *amqp.Connection
	var err error

	for i:=0; i < 10; i++ {
		conn,err = amqp.Dial(url)
		if err == nil {
			fmt.Println("Connected to MQ server")	
			return conn, nil
		}
		fmt.Printf("MQServer is not ready! Retry after 3s... %d time\n", i + 1)
		time.Sleep(3*time.Second)
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

func FailOnError(err error, msg string) {
	if err != nil {
		log.Panicf("%s: %s", msg, err)
	}
}
