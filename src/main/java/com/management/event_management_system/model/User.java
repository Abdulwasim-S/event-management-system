package com.management.event_management_system.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document(collection = "users")
public class User {

	@Id
	private String id;

	private String email;
	private String password;
	private String username;
	private String role;
}
