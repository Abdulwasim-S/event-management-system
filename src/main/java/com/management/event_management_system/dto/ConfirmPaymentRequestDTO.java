package com.management.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmPaymentRequestDTO {
	private String paymentId;
	private String orderId;
	private String signature;
	private String attendeeEmail;

}
