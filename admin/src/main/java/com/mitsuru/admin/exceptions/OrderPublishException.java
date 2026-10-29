package com.mitsuru.admin.exceptions;

import org.springframework.amqp.AmqpException;

/**
 * The broker did not confirm an order message. Unchecked, so @Transactional rolls back,
 * and an AmqpException, so GlobalExceptionHandler answers 503.
 */
public class OrderPublishException extends AmqpException {

	public OrderPublishException(String message) {
		super(message);
	}

	public OrderPublishException(String message, Throwable cause) {
		super(message, cause);
	}
}
