package org.example.authservice.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-exp-minutes}")
	private long accessExpMinutes;

	@Value("${jwt.refresh-exp-days}")
	private long refreshExpDays;

	private Key key;

	@PostConstruct
	void init() {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(Long memberId, String email, String role) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(accessExpMinutes * 60);
		return Jwts.builder()
			.setSubject(String.valueOf(memberId))
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(exp))
			.addClaims(Map.of("email", email, "role", role, "typ", "access"))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	public String createRefreshToken(Long memberId) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(refreshExpDays * 24 * 60 * 60);
		return Jwts.builder()
			.setSubject(String.valueOf(memberId))
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(exp))
			.addClaims(Map.of("typ", "refresh"))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	public Jws<Claims> parse(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
	}

}
