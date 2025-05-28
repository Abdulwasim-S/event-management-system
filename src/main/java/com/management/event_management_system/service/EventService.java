package com.management.event_management_system.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.management.event_management_system.dto.EventRequestDTO;
import com.management.event_management_system.model.Event;
import com.management.event_management_system.repository.EventRepository;

@Service
public class EventService {
	@Autowired
	private EventRepository eventRepository;

	public ResponseEntity<Object> getAllEvents() {
		return ResponseEntity.ok(eventRepository.findAll());
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

}
