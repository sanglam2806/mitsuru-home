package com.mitsuru.admin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitsuru.admin.beans.Order;
import com.mitsuru.admin.beans.OrderMessage;
import com.mitsuru.admin.messageQueue.OrderProducer;
import com.mitsuru.admin.repositories.OrderRepository;
import com.mitsuru.admin.repositories.UserRepository;

@Service
public class OrderService {
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderProducer orderProducer;
	
	public List<Order> getOrders() {
		return orderRepository.findAll();
	}

	public Order createOrder(Order order) {
			Order saveOrder =  orderRepository.save(order);

		OrderMessage message = new OrderMessage();
		message.setOrderId(order.getOrderId());
		message.setUserEmail("mitsuru@gmail.com");
		message.setNote(order.getNote());
		orderProducer.sendOrder(message);
		
		return saveOrder;
	}
}
