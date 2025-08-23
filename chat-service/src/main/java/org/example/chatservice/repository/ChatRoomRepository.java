package org.example.chatservice.repository;

import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Page<ChatRoom> findByStartPointAndEndPointAndStatus(
		Location startPoint, Location endPoint, RoomStatus status, Pageable pageable);
}
