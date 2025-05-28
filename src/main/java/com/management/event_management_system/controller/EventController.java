package com.management.event_management_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.dto.EventRequestDTO;
import com.management.event_management_system.service.EventService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin")
public class EventController {

	@Autowired
	private EventService eventService;

	@GetMapping("/event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> eventList() {
		return eventService.getAllEvents();
	}

	@PostMapping("/event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> newEvent(@RequestBody EventRequestDTO dto) {
		return eventService.postNewEvent(dto);
	}

	@PutMapping("/event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> editEvent(@RequestBody EventRequestDTO dto) {
		return eventService.updateEvent(dto);
	}

	@DeleteMapping("/event/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteEvent(@PathVariable String id) {
		return eventService.deleteEventById(id);
	}

}
