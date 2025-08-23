package org.example.chatservice.repository;

import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.KickedParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KickedParticipantRepository extends JpaRepository<KickedParticipant, Long> {
	boolean existsByChatRoomAndMemberId(ChatRoom room, Long memberId);
}