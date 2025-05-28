package com.management.event_management_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.management.event_management_system.service.UserService;

@RestController
@RequestMapping("/public/user")
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping("/all")
//	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Object> usersData() {
		try {
			return ResponseEntity.ok(userService.getUserList());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

}
