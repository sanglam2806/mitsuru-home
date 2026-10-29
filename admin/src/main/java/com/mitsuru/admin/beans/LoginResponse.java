package com.mitsuru.admin.beans;

public record LoginResponse(
	String accessToken,
	String tokenType,
	long expiresInSeconds
) {
}
