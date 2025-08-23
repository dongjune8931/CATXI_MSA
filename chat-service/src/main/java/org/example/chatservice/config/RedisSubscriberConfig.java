package org.example.chatservice.config;

import org.example.chatservice.service.RedisPubSubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

	private final RedisPubSubService listener;

	@Bean
	public RedisMessageListenerContainer redisContainer(
		org.springframework.data.redis.connection.RedisConnectionFactory cf) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(cf);
		// 채널/패턴 구독
		container.addMessageListener(listener, new PatternTopic("chat"));
		container.addMessageListener(listener, new PatternTopic("ready:*"));
		container.addMessageListener(listener, new PatternTopic("participants:*"));
		container.addMessageListener(listener, new PatternTopic("kick:*"));
		return container;
	}
}