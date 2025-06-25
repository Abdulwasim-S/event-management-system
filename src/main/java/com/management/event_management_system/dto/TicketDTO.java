package com.management.event_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
	private String ticketId;
	private String eventId;
	private String eventTitle;
	private String location;
	private String imgUrl;
	private String startTime;
	private String attendeeName;
	private String attendeeEmail;
	private String qrCode;
}
