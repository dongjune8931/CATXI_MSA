package org.example.chatservice.dto;

import java.time.LocalDateTime;

import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ChatRoomRes(
	Long roomId,
	Location startPoint,
	Location endPoint,
	Long maxCapacity,
	LocalDateTime departAt,
	RoomStatus status
) {}
