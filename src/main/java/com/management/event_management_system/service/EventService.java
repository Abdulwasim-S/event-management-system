package com.management.event_management_system.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.EventRequestDTO;
import com.management.event_management_system.model.Event;
import com.management.event_management_system.repository.EventRepository;

@Service
public class EventService {
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private MongoTemplate mongoTemplate;

	public ResponseEntity<Object> getAllEvents() {
		return ResponseEntity.ok(eventRepository.findAll());
	}

	public ResponseEntity<?> getFilteredEvents(int page, int size, String name, String location, String date) {
		try {
			Query query = new Query();
			List<Criteria> criteriaList = new ArrayList<>();

			if (name != null && !name.isEmpty()) {
				criteriaList.add(Criteria.where("title").regex(".*" + name + ".*", "i"));
			}

			if (location != null && !location.isEmpty()) {
				criteriaList.add(Criteria.where("location").regex(".*" + location + ".*", "i"));
			}

			if (date != null && !date.isEmpty()) {
				try {
					LocalDate selectedDate = LocalDate.parse(date);
					LocalDateTime start = selectedDate.atStartOfDay();
					LocalDateTime end = selectedDate.atTime(23, 59, 59);
					criteriaList.add(Criteria.where("startTime").gte(start).lte(end));
				} catch (DateTimeParseException e) {
					return ResponseEntity.badRequest().body("Invalid date format. Please use YYYY-MM-DD.");
				}
			}

			if (!criteriaList.isEmpty()) {
				query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
			}

			long total = mongoTemplate.count(query, Event.class);
			query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));
			List<Event> events = mongoTemplate.find(query, Event.class);
			Page<Event> pageResult = new PageImpl<>(events, PageRequest.of(page, size), total);

			return ResponseEntity.ok(pageResult);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch events: " + e.getMessage());
		}
	}

	public ResponseEntity<Object> postNewEvent(EventRequestDTO dto) {
		Event event = new Event();
		event.setTitle(dto.getTitle());
		event.setDescription(dto.getDescription());
		event.setLocation(dto.getLocation());
		event.setStartTime(dto.getStartTime());
		event.setEndTime(dto.getEndTime());
		event.setCategory(dto.getCategory());
		event.setMaxAttendees(dto.getMaxAttendees());
		event.setPrice(dto.getPrice());
		event.setImgUrl(dto.getImgUrl());

		Event savedEvent = eventRepository.save(event);
		return ResponseEntity.ok(savedEvent);
	}

	public ResponseEntity<?> updateEvent(EventRequestDTO dto) {
		Optional<Event> optionalEvent = eventRepository.findById(dto.getId());

		if (!optionalEvent.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		Event event = optionalEvent.get();
		event.setTitle(dto.getTitle());
		event.setDescription(dto.getDescription());
		event.setLocation(dto.getLocation());
		event.setStartTime(dto.getStartTime());
		event.setEndTime(dto.getEndTime());
		event.setCategory(dto.getCategory());
		event.setMaxAttendees(dto.getMaxAttendees());
		event.setPrice(dto.getPrice());
		event.setImgUrl(dto.getImgUrl());

		Event updatedEvent = eventRepository.save(event);
		return ResponseEntity.ok(updatedEvent);
	}

	public ResponseEntity<?> deleteEventById(String id) {
		if (!eventRepository.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		eventRepository.deleteById(id);
		return ResponseEntity.ok("Event deleted successfully");
	}

	public ResponseEntity<?> findEventById(String id) {
		try {
			Event event = eventRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

			return ResponseEntity.ok(new ApiResponseDTO<>("Event fetched successfully", event));
		} catch (Exception e) {
			return ResponseEntity.badRequest()
					.body(new ApiResponseDTO<>("Failed to fetch event: " + e.getMessage(), null));
		}
	}

}
