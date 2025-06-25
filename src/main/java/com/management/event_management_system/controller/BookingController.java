package com.management.event_management_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.BookingRequestDTO;
import com.management.event_management_system.dto.ConfirmPaymentRequestDTO;
import com.management.event_management_system.service.BookingService;
import com.management.event_management_system.service.UserService;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

	@Autowired
	private BookingService bookingService;
	@Autowired
	private UserService userService;

	@PostMapping("/create")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO request,
			@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");

		return bookingService.createBooking(request, token);
	}

	@PostMapping("/confirm")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> confirmBooking(@RequestBody ConfirmPaymentRequestDTO request) {
		return bookingService.confirmBooking(request);
	}

	@PostMapping("/cancel")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<ApiResponseDTO<?>> cancelBooking(@RequestBody Map<String, String> payload) {
		String orderId = payload.get("orderId");
		return bookingService.cancelBooking(orderId);
	}

	@GetMapping("/my-tickets")
//	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getAllUserTickets(@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return userService.getAllTicketsForUser(token);
	}

//	@GetMapping("/event/{eventId}")
//	@PreAuthorize("hasRole('ADMIN')")
//	public ResponseEntity<?> getBookingsByEventId(@PathVariable(name = "eventId") String eventId,
//			@RequestParam(name = "page", defaultValue = "0") int page,
//			@RequestParam(name = "limit", defaultValue = "5") int limit) {
//
//		return bookingService.getBookingsByEventPaginated(eventId, page, limit);
//	}

}
