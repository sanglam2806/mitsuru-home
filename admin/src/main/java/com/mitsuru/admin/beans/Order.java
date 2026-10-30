package com.mitsuru.admin.beans;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_order")
@Entity
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_seq")
	// allocationSize must match the existing sequence's INCREMENT BY (1), or Hibernate refuses to start
	@SequenceGenerator(
		name = "po_seq",
		sequenceName = "purchase_order_seq",
		allocationSize = 1
	)
	private Long orderId;

	// username of the authenticated user who placed the order
	@Column(length = 100)
	private String createdBy;

	@Column(nullable = false)
	private Long drinkId;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	private String note;

	private Instant createdAt;

}
