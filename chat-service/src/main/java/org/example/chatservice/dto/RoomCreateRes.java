package org.example.chatservice.dto;

import java.time.LocalDateTime;

import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;

public record RoomCreateRes(
	Long roomId, Location startPoint, Location endPoint,
	Long maxCapacity, LocalDateTime departAt, RoomStatus status
) {}