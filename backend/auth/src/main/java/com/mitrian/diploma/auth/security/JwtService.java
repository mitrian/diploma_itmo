package com.mitrian.diploma.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long ttlSeconds;

	public JwtService(
		@Value("${app.security.jwt.secret}") String secret,
		@Value("${app.security.jwt.ttl-seconds:86400}") long ttlSeconds
	) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.ttlSeconds = ttlSeconds;
	}

	public String generateToken(String subject) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(subject)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusSeconds(ttlSeconds)))
			.signWith(signingKey)
			.compact();
	}

	public String extractSubject(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Optional<String> parseValidSubject(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			String subject = extractAllClaims(token).getSubject();
			if (subject == null || subject.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(subject);
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	public boolean isTokenValid(String token) {
		return parseValidSubject(token).isPresent();
	}

	public long getTtlSeconds() {
		return ttlSeconds;
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
