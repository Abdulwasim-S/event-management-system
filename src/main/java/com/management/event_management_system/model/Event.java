package com.management.event_management_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "events")
public class Event {

	@Id
	private String id;

	private String title;
	private String description;
	private String location;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	private String createdBy;
	private String category;
	private BigDecimal price;

	private int maxAttendees;

}