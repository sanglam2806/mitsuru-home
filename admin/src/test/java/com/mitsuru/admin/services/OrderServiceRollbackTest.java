package com.mitsuru.admin.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mitsuru.admin.beans.CreateOrderRequest;
import com.mitsuru.admin.beans.OrderResponse;
import com.mitsuru.admin.exceptions.OrderPublishException;
import com.mitsuru.admin.messageQueue.OrderProducer;
import com.mitsuru.admin.repositories.OrderRepository;

/**
 * Runs OrderService against a real (H2) transaction to prove a failed publish rolls the INSERT back.
 */
@DataJpaTest
@Import(OrderService.class)
// @DataJpaTest wraps each test in its own transaction; turn that off so createOrder commits or rolls back for real
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
class OrderServiceRollbackTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@MockitoBean
	private OrderProducer orderProducer;

	@MockitoBean
	private CustomUserDetailService userDetailService;

	private final CreateOrderRequest request = new CreateOrderRequest(7L, new BigDecimal("4.50"), "less ice");

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		when(userDetailService.findEmail("admin")).thenReturn("admin@example.com");
	}

	@Test
	void commitsOrderWhenPublishIsConfirmed() {
		OrderResponse created = orderService.createOrder(request, "admin");

		assertThat(orderRepository.findById(created.orderId())).isPresent();
	}

	@Test
	void rollsBackOrderWhenBrokerRejectsMessage() {
		doThrow(new OrderPublishException("Broker rejected order"))
			.when(orderProducer).sendOrder(any());

		assertThatThrownBy(() -> orderService.createOrder(request, "admin"))
			.isInstanceOf(OrderPublishException.class);
		assertThat(orderRepository.count()).isZero();
	}

	@Test
	void rollsBackOrderOnUnexpectedFailureAfterInsert() {
		when(userDetailService.findEmail("admin")).thenThrow(new IllegalStateException("user lookup failed"));

		assertThatThrownBy(() -> orderService.createOrder(request, "admin"))
			.isInstanceOf(IllegalStateException.class);
		assertThat(orderRepository.count()).isZero();
	}
}
