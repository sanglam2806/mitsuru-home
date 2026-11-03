package com.mitsuru.admin.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mitsuru.admin.beans.CreateOrderRequest;
import com.mitsuru.admin.beans.OrderResponse;
import com.mitsuru.admin.services.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j 
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	public List<OrderResponse> getAll() {
		log.info("handling on {}",Thread.currentThread());	
		return orderService.getOrders();
	}

	@GetMapping("/{orderId}")
	public OrderResponse getOne(@PathVariable Long orderId) {
		return orderService.getOrder(orderId);
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
			Authentication authentication) {
		OrderResponse created = orderService.createOrder(request, authentication.getName());
		return ResponseEntity.created(URI.create("/orders/" + created.orderId())).body(created);
	}
}
