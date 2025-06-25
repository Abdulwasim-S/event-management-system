package com.management.event_management_system.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "bookings")
public class Booking {

	@Id
	private String id;

	private String eventId;
	private String attendeeName;
	private String attendeeEmail;
	private String attendeeId;

	private String paymentId;
	private String orderId;
	private String status;

	private LocalDateTime bookedAt = LocalDateTime.now(); // Optional
}
