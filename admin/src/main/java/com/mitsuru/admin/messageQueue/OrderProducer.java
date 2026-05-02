package com.mitsuru.admin.messageQueue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mitsuru.admin.beans.OrderMessage;

@Component
public class OrderProducer {

	@Autowired
	private AmqpTemplate amqpTemplate;

	public void sendOrder(OrderMessage message){
		amqpTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
		System.out.println("Order sent to RabbitMQ: " + message);
	}
	
}
