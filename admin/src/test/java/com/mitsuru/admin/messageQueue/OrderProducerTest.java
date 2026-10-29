package com.mitsuru.admin.messageQueue;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.mitsuru.admin.beans.OrderMessage;
import com.mitsuru.admin.exceptions.OrderPublishException;

class OrderProducerTest {

	private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
	private final OrderProducer producer = new OrderProducer(rabbitTemplate, 200);
	private final OrderMessage message = new OrderMessage(1L, "admin@example.com", "less ice");

	/** Simulates what the broker does with the CorrelationData passed to convertAndSend. */
	private void brokerAnswers(Consumer<CorrelationData> broker) {
		doAnswer(invocation -> {
			broker.accept(invocation.getArgument(3));
			return null;
		}).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
	}

	@Test
	void succeedsWhenBrokerAcks() {
		brokerAnswers(cd -> cd.getFuture().complete(new CorrelationData.Confirm(true, null)));

		assertThatCode(() -> producer.sendOrder(message)).doesNotThrowAnyException();
	}

	@Test
	void failsWhenBrokerNacks() {
		brokerAnswers(cd -> cd.getFuture().complete(new CorrelationData.Confirm(false, "queue full")));

		assertThatThrownBy(() -> producer.sendOrder(message))
			.isInstanceOf(OrderPublishException.class)
			.hasMessageContaining("queue full");
	}

	@Test
	void failsWhenMessageIsUnroutable() {
		brokerAnswers(cd -> {
			cd.setReturned(new ReturnedMessage(new Message(new byte[0], new MessageProperties()),
				312, "NO_ROUTE", RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY));
			cd.getFuture().complete(new CorrelationData.Confirm(true, null));
		});

		assertThatThrownBy(() -> producer.sendOrder(message))
			.isInstanceOf(OrderPublishException.class)
			.hasMessageContaining("NO_ROUTE");
	}

	@Test
	void failsWhenBrokerDoesNotAnswerInTime() {
		brokerAnswers(cd -> { /* no confirm */ });

		assertThatThrownBy(() -> producer.sendOrder(message))
			.isInstanceOf(OrderPublishException.class)
			.hasMessageContaining("within 200 ms");
	}
}
