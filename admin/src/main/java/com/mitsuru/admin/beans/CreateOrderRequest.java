package com.mitsuru.admin.beans;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
	@NotNull @Positive Long drinkId,
	@NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
	@Size(max = 255) String note
) {
}
