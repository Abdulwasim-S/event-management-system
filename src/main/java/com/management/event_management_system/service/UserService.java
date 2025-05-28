package com.management.event_management_system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.management.event_management_system.config.JwtUtil;
import com.management.event_management_system.dto.ApiResponseDTO;
import com.management.event_management_system.dto.LoginRequestDTO;
import com.management.event_management_system.dto.LoginResponseDTO;
import com.management.event_management_system.model.User;
import com.management.event_management_system.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;

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

}
