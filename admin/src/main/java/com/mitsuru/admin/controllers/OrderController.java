package com.mitsuru.admin.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mitsuru.admin.beans.Order;
import com.mitsuru.admin.beans.OrderMessage;
import com.mitsuru.admin.services.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@GetMapping("/mitsuru")
	public ResponseEntity<String> helloWorld(){
		return ResponseEntity.ok("Hello Na-chan from with love");
	} 

	@GetMapping("/create")
	public ResponseEntity<Order> createOrder(@RequestBody Order order) {
		return ResponseEntity.ok(orderService.createOrder(order));
	}

	@GetMapping("/listall")
	public ResponseEntity<List<Order>> getAll() {
		return ResponseEntity.ok(orderService.getOrders());
	}
}
