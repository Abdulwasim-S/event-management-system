package com.management.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {
	private String eventId;
	private String attendeeName;
	private String attendeeEmail;
}
