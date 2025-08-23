package org.example.chatservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
	@Bean
	public RedisConnectionFactory redisConnectionFactory() { return new LettuceConnectionFactory(); }

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) { return new StringRedisTemplate(cf); }

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
		RedisTemplate<String, String> t = new RedisTemplate<>();
		t.setConnectionFactory(cf);
		return t;
	}
}