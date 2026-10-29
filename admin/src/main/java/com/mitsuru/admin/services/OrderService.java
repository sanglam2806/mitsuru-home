package com.mitsuru.admin.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mitsuru.admin.beans.CreateOrderRequest;
import com.mitsuru.admin.beans.Order;
import com.mitsuru.admin.beans.OrderMessage;
import com.mitsuru.admin.beans.OrderResponse;
import com.mitsuru.admin.exceptions.OrderNotFoundException;
import com.mitsuru.admin.messageQueue.OrderProducer;
import com.mitsuru.admin.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderProducer orderProducer;
	private final CustomUserDetailService userDetailService;

	@Transactional(readOnly = true)
	public List<OrderResponse> getOrders() {
		return orderRepository.findAll().stream().map(OrderResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.map(OrderResponse::from)
			.orElseThrow(() -> new OrderNotFoundException(orderId));
	}

	/**
	 * Saves the order and publishes it for the email service, all or nothing:
	 * <ol>
	 *   <li>INSERT and flush, so database errors happen before anything is sent</li>
	 *   <li>publish and wait for the broker confirm; a failure throws and rolls the INSERT back</li>
	 *   <li>commit</li>
	 * </ol>
	 * The one gap left is a commit failure after a confirmed publish (e.g. the DB connection drops).
	 * That is logged as an orphan message; a transactional outbox would close it.
	 */
	@Transactional(rollbackFor = Exception.class)
	public OrderResponse createOrder(CreateOrderRequest request, String username) {
		Order order = new Order();
		order.setCreatedBy(username);
		order.setDrinkId(request.drinkId());
		order.setPrice(request.price());
		order.setNote(request.note());
		order.setCreatedAt(Instant.now());

		Order saved = orderRepository.saveAndFlush(order);

		orderProducer.sendOrder(new OrderMessage(
			saved.getOrderId(),
			userDetailService.findEmail(username),
			saved.getNote()));
		warnIfRolledBackAfterPublish(saved.getOrderId());

		return OrderResponse.from(saved);
	}

	private void warnIfRolledBackAfterPublish(Long orderId) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					log.error("Order {} was published but its transaction did not commit; "
						+ "the email service received a message for an order that does not exist", orderId);
				}
			}
		});
	}
}
