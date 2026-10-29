package com.metro.authservice.util;

import java.nio.charset.StandardCharsets; // NEW import
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	// converts your plain-text secret string into a SecretKey the HMAC algorithm can use
	public SecretKey getSigningKey() {
		// CHANGED: explicit UTF-8 (getBytes() alone uses the machine's default charset).
		// Station already uses UTF-8, so both sides now always produce the same key bytes.
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(Long userId, String email, String role) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
				.subject(email)            // subject is the EMAIL
				.claim("userId", userId)   // the user id is in this custom claim: Booking must read "userID"
				.claim("role", role)       // "USER" or "ADMIN", matches Station's filter
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(getSigningKey())
				.compact();
	}

	public Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = extractClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}