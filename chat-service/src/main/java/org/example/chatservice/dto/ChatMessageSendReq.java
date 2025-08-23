package org.example.chatservice.dto;

import java.time.LocalDateTime;

public record ChatMessageSendReq(
	Long roomId, Long senderId, String senderEmail,
	String message, LocalDateTime sentAt
) {}
