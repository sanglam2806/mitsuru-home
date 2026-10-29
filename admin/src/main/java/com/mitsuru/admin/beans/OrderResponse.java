package com.mitsuru.admin.beans;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
	Long orderId,
	String createdBy,
	Long drinkId,
	BigDecimal price,
	String note,
	Instant createdAt
) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
			order.getOrderId(),
			order.getCreatedBy(),
			order.getDrinkId(),
			order.getPrice(),
			order.getNote(),
			order.getCreatedAt()
		);
	}
}
