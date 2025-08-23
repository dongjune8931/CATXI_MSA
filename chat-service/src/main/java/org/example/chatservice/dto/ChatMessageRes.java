package org.example.chatservice.dto;

import java.time.LocalDateTime;

public record ChatMessageRes(
	Long messageId, Long roomId, Long senderId, String senderNickname,
	String content, LocalDateTime createdAt
) {}