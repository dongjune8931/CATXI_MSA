package org.example.chatservice.controller;

import java.time.LocalDateTime;

import org.example.chatservice.dto.ChatMessageSendReq;
import org.example.chatservice.service.ChatMessageService;
import org.example.chatservice.service.RedisPubSubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StompController {

	private final SimpMessageSendingOperations messaging;
	private final ChatMessageService chatMessageService;
	private final RedisPubSubService pubsub;
	private final ObjectMapper om = new ObjectMapper();

	@MessageMapping("/room.{roomId}.send")
	public void send(@DestinationVariable Long roomId,
		@Payload String message,
		Authentication authentication) throws Exception {
		Jwt jwt = (Jwt) authentication.getPrincipal();
		Long memberId = Long.valueOf(jwt.getSubject());
		String email = (String) jwt.getClaims().getOrDefault("email", "unknown@catxi");

		ChatMessageSendReq dto = new ChatMessageSendReq(roomId, memberId, email, message, LocalDateTime.now());
		chatMessageService.saveMessage(roomId, dto);

		// Redis pub-sub fanout
		pubsub.publish("chat", om.writeValueAsString(dto));
	}
}