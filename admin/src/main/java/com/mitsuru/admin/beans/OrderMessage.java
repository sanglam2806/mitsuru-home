package com.mitsuru.admin.beans;

public record OrderMessage (
	Long orderId,
	String userEmail,
	String note
) {}
