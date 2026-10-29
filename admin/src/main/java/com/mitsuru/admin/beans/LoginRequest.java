package com.mitsuru.admin.beans;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank String username,
	@NotBlank String password
) {
}
