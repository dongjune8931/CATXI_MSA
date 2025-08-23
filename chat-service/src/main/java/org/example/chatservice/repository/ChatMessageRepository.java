package org.example.chatservice.repository;

import java.util.List;

import org.example.chatservice.domain.ChatMessage;
import org.example.chatservice.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom room);
	void deleteAllByChatRoom(ChatRoom room);
}