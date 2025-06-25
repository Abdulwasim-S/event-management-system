package com.management.event_management_system.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.management.event_management_system.config.JwtUtil;
import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.LoginRequestDTO;
import com.management.event_management_system.dto.LoginResponseDTO;
import com.management.event_management_system.dto.TicketDTO;
import com.management.event_management_system.model.Booking;
import com.management.event_management_system.model.Event;
import com.management.event_management_system.model.User;
import com.management.event_management_system.repository.BookingRepository;
import com.management.event_management_system.repository.EventRepository;
import com.management.event_management_system.repository.UserRepository;
import com.management.event_management_system.util.QRCodeUtil;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private BookingRepository bookingRepository;
	@Autowired
	private QRCodeUtil qrCodeUtil;

	public ResponseEntity<Object> getUserList() {
		List<User> users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	public ResponseEntity<Object> getUserDetails(String email) {
		Optional<User> user = userRepository.findByEmail(email);
		if (user.isPresent()) {
			return ResponseEntity.ok(user.get());
		} else {
			return ResponseEntity.status(404).body("User not found");
		}
	}

	public ResponseEntity<Object> login(LoginRequestDTO request) {
		Optional<User> user = userRepository.findByEmail(request.getEmail());

		if (!user.isPresent() || !passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
			return ResponseEntity.status(401).body(new ApiResponseDTO<>("Invalid email or password", null));
		}

		String token = jwtUtil.generateToken(user.get().getEmail(), user.get().getRole());
		return ResponseEntity.ok(new LoginResponseDTO("Login Successful", token));
	}

	public ResponseEntity<Object> signup(User request) {
		Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
		if (existingUser.isPresent()) {
			return ResponseEntity.status(409).body(new ApiResponseDTO<>("Email is already registered", null));
		}

		request.setPassword(passwordEncoder.encode(request.getPassword()));

		request.setRole("USER");

		User savedUser = userRepository.save(request);

		String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

		Map<String, String> result = new HashMap<>();
		result.put("token", token);

		return ResponseEntity.ok(new ApiResponseDTO<>("User registered successfully", result));
	}

	public ResponseEntity<?> getAllEvents(int page, int size, String search, String category) {
		Pageable pageable = PageRequest.of(page, size);

		String titleFilter = (search == null || search.trim().isEmpty()) ? ".*"
				: ".*" + Pattern.quote(search.trim()) + ".*";

		String categoryFilter = (category == null || category.equalsIgnoreCase("all") || category.trim().isEmpty())
				? ".*"
				: ".*" + Pattern.quote(category.trim()) + ".*";

		Date currentTime = new Date(); // current time to filter out past events

		Page<Event> eventsPage = eventRepository.searchEvents(titleFilter, categoryFilter, currentTime, pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("content", eventsPage.getContent());
		response.put("number", eventsPage.getNumber());
		response.put("last", eventsPage.isLast());
		response.put("totalElements", eventsPage.getTotalElements());
		response.put("totalPages", eventsPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

	public Page<User> getUsers(int page, int size, String search) {
		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

			List<User> allUsers = userRepository.findAll();

			List<User> filtered = allUsers.stream()
					.filter(user -> (search == null || search.isBlank())
							|| user.getUsername().toLowerCase().contains(search.toLowerCase())
							|| user.getEmail().toLowerCase().contains(search.toLowerCase()))
					.toList();

			int start = Math.min((int) pageable.getOffset(), filtered.size());
			int end = Math.min(start + pageable.getPageSize(), filtered.size());

			List<User> paginatedList = filtered.subList(start, end);
			return new PageImpl<>(paginatedList, pageable, filtered.size());

		} catch (Exception e) {
			return Page.empty();
		}
	}

	public ResponseEntity<?> getAllTicketsForUser(String token) {
		try {
			String email = jwtUtil.getEmailFromToken(token);

			String attendeeId = userRepository.findByEmail(email).map(User::getId)
					.orElseThrow(() -> new RuntimeException("User not found with email: " + email));

			List<Booking> bookings = bookingRepository.findAllByAttendeeIdAndStatus(attendeeId, "CONFIRMED");

			if (bookings.isEmpty()) {
				return ResponseEntity.ok(new ApiResponseDTO<>("No confirmed bookings found", new ArrayList<>()));
			}

			List<TicketDTO> tickets = new ArrayList<>();

			for (Booking booking : bookings) {
				Optional<Event> eventOpt = eventRepository.findById(booking.getEventId());
				if (eventOpt.isEmpty())
					continue;

				Event event = eventOpt.get();
				String qrText = "TicketID:" + booking.getId() + ",EventID:" + event.getId() + ",Name:"
						+ booking.getAttendeeName();

				byte[] qrBytes = qrCodeUtil.generateQRCodeImage(qrText, 300, 300);
				String qrBase64 = Base64.getEncoder().encodeToString(qrBytes);

				tickets.add(new TicketDTO(booking.getId(), event.getId(), event.getTitle(), event.getLocation(),
						event.getImgUrl(), event.getStartTime().toString(), booking.getAttendeeName(),
						booking.getAttendeeEmail(), "data:image/png;base64," + qrBase64));
			}

			return ResponseEntity.ok(new ApiResponseDTO<>("Tickets fetched successfully", tickets));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponseDTO<>("Error: " + e.getMessage(), null));
		}
	}

}
