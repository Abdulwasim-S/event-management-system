package com.management.event_management_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.DashboardStatsDTO;
import com.management.event_management_system.model.User;
import com.management.event_management_system.service.BookingService;
import com.management.event_management_system.service.DashboardService;
import com.management.event_management_system.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private BookingService bookingService;

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Object> getUsers(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size,
			@RequestParam(name = "search", required = false) String search) {
		Page<User> users = userService.getUsers(page, size, search);
		ApiResponseDTO<Page<User>> response = new ApiResponseDTO<>("User list fetched successfully", users);
		return ResponseEntity.ok(response);

	}

	@GetMapping("/dashboard")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getDashboardData() {
		DashboardStatsDTO stats = dashboardService.getDashboardStats();
		return ResponseEntity.ok(stats);
	}

	@GetMapping("/event/{eventId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getBookingsByEventId(@PathVariable(name = "eventId") String eventId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "limit", defaultValue = "5") int limit) {

		return bookingService.getBookingsByEventPaginated(eventId, page, limit);
	}

}
