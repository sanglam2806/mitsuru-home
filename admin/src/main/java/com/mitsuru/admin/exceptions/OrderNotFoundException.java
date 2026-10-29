package com.mitsuru.admin.exceptions;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(Long orderId) {
		super("Order " + orderId + " not found");
	}
}
