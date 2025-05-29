package com.management.event_management_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.dto.LoginRequestDTO;
import com.management.event_management_system.model.User;
import com.management.event_management_system.service.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@GetMapping("/start")
	public ResponseEntity<String> startApp() {
		return ResponseEntity.ok("Server is running");
	}

	@PostMapping("/login")
	public ResponseEntity<Object> UserLogin(@RequestBody LoginRequestDTO request) {
		return userService.login(request);
	}

	@PostMapping("/signup")
	public ResponseEntity<Object> UserSignup(@RequestBody User request) {
		return userService.signup(request);
	}

}
