package com.management.event_management_system.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final String secret = "mysecretkeymysecretkeymysecretkeymysecretkey";

	private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());

	public String generateToken(String username, String role) {
		long expirationMillis = 1000L * 60 * 60 * 10; // 10 hours
		return Jwts.builder().subject(username).claim("role", role).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationMillis)).signWith(SECRET_KEY).compact();
	}

	public SecretKey getSecretKey() {

		return SECRET_KEY;
	}

}
