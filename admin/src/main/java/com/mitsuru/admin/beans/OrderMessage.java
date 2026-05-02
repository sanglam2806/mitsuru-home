package com.mitsuru.admin.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {

	private Long orderId;

	private String userEmail;

	private String note;
	
}
