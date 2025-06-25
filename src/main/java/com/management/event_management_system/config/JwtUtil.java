package com.management.event_management_system.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey SECRET_KEY;

	@PostConstruct
	public void init() {
		this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateToken(String username, String role) {
		long expirationMillis = 1000L * 60 * 60 * 10; // 10 hours
		return Jwts.builder().subject(username).claim("role", role).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationMillis)).signWith(SECRET_KEY).compact();
	}

	public String getEmailFromToken(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
			return claims.getSubject();
		} catch (JwtException e) {
			System.err.println("Failed to parse JWT token: " + e.getMessage());
			throw new RuntimeException("Invalid token", e);
		}
	}

	public SecretKey getSecretKey() {

		return SECRET_KEY;
	}

}
