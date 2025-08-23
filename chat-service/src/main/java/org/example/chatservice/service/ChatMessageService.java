package org.example.chatservice.service;



import lombok.RequiredArgsConstructor;

import org.example.chatservice.domain.ChatMessage;
import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.MessageType;
import org.example.chatservice.dto.ChatMessageRes;
import org.example.chatservice.dto.ChatMessageSendReq;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.repository.ChatParticipantRepository;
import org.example.chatservice.repository.ChatRoomRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatParticipantRepository chatParticipantRepository;

	private final StringRedisTemplate redis; // ✅ 발행은 직접 수행


	public void saveMessage(Long roomId, ChatMessageSendReq req) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		// 멤버십 확인
		chatParticipantRepository.findByChatRoomAndMemberId(room, req.senderId())
			.orElseThrow(() -> new IllegalArgumentException("Not a participant"));

		ChatMessage chat = ChatMessage.builder()
			.chatRoom(room)
			.senderId(req.senderId())
			.senderNickname(null) // 필요시 UserService 조회로 채움
			.content(req.message())
			.msgType(MessageType.CHAT)
			.build();
		chatMessageRepository.save(chat);
	}

	@Transactional(readOnly = true)
	public List<ChatMessageRes> getChatHistory(Long roomId, Long memberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		chatParticipantRepository.findByChatRoomAndMemberId(room, memberId)
			.orElseThrow(() -> new IllegalArgumentException("Not a participant"));

		return chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(room)
			.stream()
			.map(m -> new ChatMessageRes(
				m.getId(), room.getRoomId(), m.getSenderId(), m.getSenderNickname(),
				m.getContent(), m.getCreatedTime()
			)).toList();
	}

	public void sendSystemMessage(Long roomId, String content){
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		ChatMessage systemMsg = ChatMessage.builder()
			.chatRoom(chatRoom)
			.senderId(null)
			.senderNickname("[SYSTEM]")
			.content(content)
			.msgType(MessageType.SYSTEM)
			.build();
		chatMessageRepository.save(systemMsg);

		ChatMessageSendReq dto = new ChatMessageSendReq(
			roomId, null, "[SYSTEM]", content, java.time.LocalDateTime.now()
		);

		try {
			String json = objectMapper().writeValueAsString(dto);
			redis.convertAndSend("chat", json); // ✅ 직접 발행
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new RuntimeException("시스템 메시지 직렬화 실패", e);
		}
	}

	private com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
		return new com.fasterxml.jackson.databind.ObjectMapper()
			.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
			.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
