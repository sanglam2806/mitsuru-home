package com.mitsuru.admin.messageQueue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mitsuru.admin.beans.OrderMessage;
import com.mitsuru.admin.exceptions.OrderPublishException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderProducer {

	private final RabbitTemplate rabbitTemplate;
	private final long confirmTimeoutMs;

	public OrderProducer(RabbitTemplate rabbitTemplate,
			@Value("${app.rabbitmq.confirm-timeout-ms:5000}") long confirmTimeoutMs) {
		this.rabbitTemplate = rabbitTemplate;
		this.confirmTimeoutMs = confirmTimeoutMs;
	}

	public void sendOrder(OrderMessage message){
		CorrelationData correlation = new CorrelationData(String.valueOf(message.orderId()));
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message, correlation);

		CorrelationData.Confirm confirm;
		try {
			confirm = correlation.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			// the message may still arrive; the consumer must tolerate an order id that was rolled back
			throw new OrderPublishException("No broker confirm for order " + message.orderId()
				+ " within " + confirmTimeoutMs + " ms", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OrderPublishException("Interrupted waiting for confirm of order " + message.orderId(), e);
		} catch (ExecutionException e) {
			throw new OrderPublishException("Failed to publish order " + message.orderId(), e.getCause());
		}

		if (!confirm.ack()) {
			throw new OrderPublishException("Broker rejected order " + message.orderId() + ": " + confirm.reason());
		}
		// with mandatory=true an unroutable message is returned before the (positive) confirm arrives
		if (correlation.getReturned() != null) {
			throw new OrderPublishException("Order " + message.orderId() + " was not routed to any queue: "
				+ correlation.getReturned().getReplyText());
		}

		log.info("Order {} confirmed by RabbitMQ", message.orderId());
	}

}
