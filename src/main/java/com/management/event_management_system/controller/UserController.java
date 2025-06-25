package com.management.event_management_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.service.EventService;
import com.management.event_management_system.service.UserService;

@RestController
@RequestMapping("/public/user")
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private EventService eventService;

	@GetMapping("/event")
//	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getAllEvents(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "category", required = false) String category) {
		return userService.getAllEvents(page, size, search, category);
	}

	@GetMapping("/data/{id}")
//	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getEventById(@PathVariable("id") String id) {
		System.out.println("Received ID: " + id);
		return eventService.findEventById(id);
	}

}
