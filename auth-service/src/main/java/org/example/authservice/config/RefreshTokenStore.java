package org.example.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

	private final StringRedisTemplate redis;

	@Value("${jwt.refresh-exp-days}")
	private long refreshExpDays;

	private String key(Long memberId) { return "auth:refresh:" + memberId; }

	public void save(Long memberId, String refreshToken) {
		redis.opsForValue().set(key(memberId), refreshToken, Duration.ofDays(refreshExpDays));
	}

	public String get(Long memberId) {
		return redis.opsForValue().get(key(memberId));
	}

	public void delete(Long memberId) {
		redis.delete(key(memberId));
	}
}