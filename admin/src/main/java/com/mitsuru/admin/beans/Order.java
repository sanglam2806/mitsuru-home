package com.mitsuru.admin.beans;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table(name = "purchase_order")
@Entity
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@SequenceGenerator(
		name = "po_seq",
		sequenceName = "purchase_order_seq", 
		allocationSize = 1
	)
	@Getter @Setter 
	private Long orderId;

	@Getter @Setter
	private Long userId;

	@Getter @Setter
	private Long drinkId;

	@Getter @Setter
	private BigDecimal price;

	@Getter @Setter
	private String note;

	public Order(Long orderId, Long userId, Long drinkId, BigDecimal price, String note) {
		this.orderId = orderId;
		this.userId = userId;
		this.drinkId = drinkId;
		this.price = price;
		this.note = note;
	}
	
}
