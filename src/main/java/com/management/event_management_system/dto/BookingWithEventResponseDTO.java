package com.management.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingWithEventResponseDTO {
	private String bookingId;
	private String attendeeName;
	private String attendeeEmail;
	private String paymentId;
	private String status;
	private String eventId;
	private String eventTitle;
	private String eventStartTime;
	private String eventEndTime;
	private String eventLocation;
	private String eventPrice;
}
