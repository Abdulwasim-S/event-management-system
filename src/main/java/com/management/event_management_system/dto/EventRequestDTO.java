package com.management.event_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestDTO {
	private String id;
	private String title;
	private String description;
	private String location;
	private String imgUrl;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String category;
	private int maxAttendees;
	private BigDecimal price;
}
