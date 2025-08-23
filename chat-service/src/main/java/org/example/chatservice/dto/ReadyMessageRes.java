package org.example.chatservice.dto;

import java.time.LocalDateTime;

import org.example.chatservice.domain.ENUM.ReadyType;

public record ReadyMessageRes(
	Long roomId,
	Long memberId,
	String nickname,
	ReadyType type,
	LocalDateTime occurredAt
) {
	public static ReadyMessageRes readyRequest(Long roomId, Long hostId, String hostNickname) {
		return new ReadyMessageRes(roomId, hostId, hostNickname, ReadyType.READY_REQUEST, LocalDateTime.now());
	}

	public static ReadyMessageRes readyAccept(Long roomId, Long memberId, String nickname) {
		return new ReadyMessageRes(roomId, memberId, nickname, ReadyType.READY_ACCEPT, LocalDateTime.now());
	}

	public static ReadyMessageRes readyDeny(Long roomId, Long memberId, String nickname) {
		return new ReadyMessageRes(roomId, memberId, nickname, ReadyType.READY_DENY, LocalDateTime.now());
	}
}