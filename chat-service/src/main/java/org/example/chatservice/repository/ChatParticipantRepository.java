package org.example.chatservice.repository;

import java.util.List;
import java.util.Optional;

import org.example.chatservice.domain.ChatParticipant;
import org.example.chatservice.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
	boolean existsByMemberId(Long memberId);
	long countByChatRoom(ChatRoom room);
	Optional<ChatParticipant> findByChatRoomAndMemberId(ChatRoom room, Long memberId);
	Optional<ChatParticipant> findByMemberId(Long memberId);
	List<ChatParticipant> findByChatRoom(ChatRoom room);

	void deleteAllByChatRoomAndIsReadyFalse(ChatRoom room);

	long countByChatRoomAndIsReady(ChatRoom room, boolean isReady);

	// 호스트 제외 전원 레디 false
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update ChatParticipant p set p.isReady=false " +
		"where p.chatRoom.roomId = :roomId and p.isHost = false")
	int updateIsReadyFalseExceptHost(@Param("roomId") Long roomId);

	// 미준비 인원(호스트 제외) 모두 제거
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from ChatParticipant p " +
		"where p.chatRoom = :room and p.isReady = false and p.isHost = false")
	int deleteAllNotReadyExceptHost(@Param("room") ChatRoom room);
}