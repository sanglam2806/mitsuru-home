package com.mitsuru.admin.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.mitsuru.admin.beans.CreateOrderRequest;
import com.mitsuru.admin.beans.OrderResponse;
import com.mitsuru.admin.exceptions.OrderNotFoundException;
import com.mitsuru.admin.securites.PasswordConfig;
import com.mitsuru.admin.securites.ServerSecurity;
import com.mitsuru.admin.services.AuthService;
import com.mitsuru.admin.services.CustomUserDetailService;
import com.mitsuru.admin.services.JwtService;
import com.mitsuru.admin.services.OrderService;

@WebMvcTest(controllers = { OrderController.class, LoginController.class })
@Import({ ServerSecurity.class, PasswordConfig.class, JwtService.class, AuthService.class, CustomUserDetailService.class })
@TestPropertySource(properties = {
	"app.jwt.secret=test-secret-that-is-at-least-32-characters-long",
	"app.jwt.expiration-ms=60000",
	"app.admin.username=admin",
	"app.admin.password=secret",
	"app.admin.email=admin@example.com"
})
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderService orderService;

	private static final OrderResponse SAMPLE = new OrderResponse(
		1L, "admin", 7L, new BigDecimal("4.50"), "less ice", Instant.parse("2026-10-25T00:00:00Z"));

	private String login() throws Exception {
		String body = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"secret\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(body, "$.accessToken");
	}

	@Test
	void loginTwiceWorks() throws Exception {
		// guards against Spring Security erasing the cached admin password after the first login
		login();
		login();
	}

	@Test
	void loginWithWrongPasswordReturns401() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginWithUnknownUserReturns401() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"someone\",\"password\":\"secret\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginWithBlankFieldsReturns400() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void ordersWithoutTokenReturns401() throws Exception {
		mockMvc.perform(get("/orders"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void ordersWithInvalidTokenReturns401() throws Exception {
		mockMvc.perform(get("/orders").header("Authorization", "Bearer not-a-jwt"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void listOrders() throws Exception {
		when(orderService.getOrders()).thenReturn(List.of(SAMPLE));

		mockMvc.perform(get("/orders").header("Authorization", login()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].orderId").value(1));
	}

	@Test
	void getMissingOrderReturns404() throws Exception {
		when(orderService.getOrder(99L)).thenThrow(new OrderNotFoundException(99L));

		mockMvc.perform(get("/orders/99").header("Authorization", login()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Order 99 not found"));
	}

	@Test
	void createOrderUsesAuthenticatedUser() throws Exception {
		when(orderService.createOrder(any(CreateOrderRequest.class), eq("admin"))).thenReturn(SAMPLE);

		mockMvc.perform(post("/orders")
				.header("Authorization", login())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"drinkId\":7,\"price\":4.50,\"note\":\"less ice\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/orders/1"))
			.andExpect(jsonPath("$.createdBy").value("admin"));
	}

	@Test
	void createOrderWithInvalidBodyReturns400() throws Exception {
		mockMvc.perform(post("/orders")
				.header("Authorization", login())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"drinkId\":null,\"price\":-1}"))
			.andExpect(status().isBadRequest());

		verify(orderService, never()).createOrder(any(), any());
	}
}
