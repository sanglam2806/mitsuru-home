package com.mitsuru.admin.messageQueue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	
	public final static String QUEUE = "order.queue";
	public final static String EXCHANGE = "order.exchange";
	public final static String ROUTING_KEY = "order.routing";

	@Bean
	public Queue queue() {
		// true = durable, survices start
		return new Queue(QUEUE, true);
	}

	@Bean
	public DirectExchange directExchange(){
		return new DirectExchange(EXCHANGE);
	}

	@Bean
	public Binding binding(Queue queue, DirectExchange directExchange) {
		return BindingBuilder.bind(queue).to(directExchange).with(ROUTING_KEY);
	}

	@Bean
	public MessageConverter messageConverter(){
		// send message as JSON
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter());
		return template;
	}
	
}
