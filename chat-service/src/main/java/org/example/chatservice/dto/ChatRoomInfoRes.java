package org.example.chatservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;

public record ChatRoomInfoRes(
	Long roomId, Long hostId, String hostNickname,
	Location startPoint, Location endPoint,
	LocalDateTime departAt, RoomStatus status,
	List<String> participantNicknames
) {}
