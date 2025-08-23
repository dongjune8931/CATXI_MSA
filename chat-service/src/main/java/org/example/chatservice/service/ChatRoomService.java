package org.example.chatservice.service;


import lombok.RequiredArgsConstructor;

import org.example.chatservice.domain.ChatParticipant;
import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;
import org.example.chatservice.domain.KickedParticipant;
import org.example.chatservice.dto.ChatRoomInfoRes;
import org.example.chatservice.dto.ChatRoomRes;
import org.example.chatservice.dto.RoomCreateReq;
import org.example.chatservice.dto.RoomCreateRes;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.repository.ChatParticipantRepository;
import org.example.chatservice.repository.ChatRoomRepository;
import org.example.chatservice.repository.KickedParticipantRepository;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatParticipantRepository chatParticipantRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final KickedParticipantRepository kickedParticipantRepository;
	private final ChatMessageService chatMessageService;
	private final StringRedisTemplate stringRedis;

	public RoomCreateRes createRoom(RoomCreateReq req, Long hostId, String hostNickname) {
		if (req.startPoint().equals(req.endPoint()))
			throw new IllegalArgumentException("start=end not allowed");

		// 한 유저는 하나의 방만 (필요 시 호스트만 제한)
		if (chatParticipantRepository.existsByMemberId(hostId))
			throw new IllegalArgumentException("Already in another room");

		ChatRoom room = ChatRoom.builder()
			.hostId(hostId).hostNickname(hostNickname)
			.startPoint(req.startPoint()).endPoint(req.endPoint())
			.departAt(req.departAt()).status(RoomStatus.WAITING)
			.maxCapacity(req.recruitSize()).build();
		chatRoomRepository.save(room);

		ChatParticipant host = ChatParticipant.builder()
			.chatRoom(room).memberId(hostId).nickname(hostNickname)
			.isHost(true).isReady(true).build();
		chatParticipantRepository.save(host);

		return new RoomCreateRes(room.getRoomId(), room.getStartPoint(), room.getEndPoint(),
			room.getMaxCapacity(), room.getDepartAt(), room.getStatus());
	}

	public Page<ChatRoomRes> getChatRoomList(String direction, String station, String sort, Integer page) {
		// 간단 예시: 역/방향 별로 START/END 조합 사용
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
		Location start = "SOSA_ST" .equals(direction) ? Location.SOSA_ST: Location.valueOf(station);
		Location end   = "YEOKGOK_ST".equals(direction) ? Location.valueOf(station) : Location.YEOKGOK_ST;
		return chatRoomRepository.findByStartPointAndEndPointAndStatus(start, end, RoomStatus.WAITING, pageable)
			.map(r -> new ChatRoomRes(
				r.getRoomId(), r.getStartPoint(), r.getEndPoint(),
				r.getMaxCapacity(), r.getDepartAt(), r.getStatus()));
	}

	public void leaveChatRoom(Long roomId, Long memberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		ChatParticipant part = chatParticipantRepository.findByChatRoomAndMemberId(room, memberId)
			.orElseThrow(() -> new IllegalArgumentException("Not a participant"));

		if (part.isHost()) {
			chatMessageRepository.deleteAllByChatRoom(room);
			chatRoomRepository.delete(room);
			return;
		}
		chatParticipantRepository.delete(part);
		chatMessageService.sendSystemMessage(roomId, "사용자 퇴장");
	}

	public void joinChatRoom(Long roomId, Long memberId, String nickname) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		if (kickedParticipantRepository.existsByChatRoomAndMemberId(room, memberId))
			throw new IllegalArgumentException("Blocked from room");

		if (room.getStatus() != RoomStatus.WAITING)
			throw new IllegalArgumentException("Room not waiting");

		long current = chatParticipantRepository.countByChatRoom(room);
		if (current >= room.getMaxCapacity() + 1) // +host
			throw new IllegalArgumentException("Room full");

		ChatParticipant p = ChatParticipant.builder()
			.chatRoom(room).memberId(memberId).nickname(nickname).build();
		chatParticipantRepository.save(p);

		chatMessageService.sendSystemMessage(roomId, nickname + " 입장");
	}

	public Long getMyChatRoomId(Long memberId) {
		return chatParticipantRepository.findByMemberId(memberId)
			.orElseThrow(() -> new IllegalArgumentException("Not in room"))
			.getChatRoom().getRoomId();
	}

	public void kickUser(Long roomId, Long requesterId, Long targetMemberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		if (!Objects.equals(room.getHostId(), requesterId))
			throw new IllegalArgumentException("Not host");

		ChatParticipant participant = chatParticipantRepository.findByChatRoomAndMemberId(room, targetMemberId)
			.orElseThrow(() -> new IllegalArgumentException("Target not participant"));

		chatParticipantRepository.delete(participant);

		KickedParticipant kicked = KickedParticipant.builder()
			.chatRoom(room).memberId(targetMemberId).build();
		kickedParticipantRepository.save(kicked);

		chatMessageService.sendSystemMessage(roomId, "강퇴되었습니다.");
		stringRedis.convertAndSend("kick:" + targetMemberId, "KICKED");
	}

	public boolean isRoomParticipant(Long memberId, Long roomId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		return chatParticipantRepository.findByChatRoom(room).stream()
			.anyMatch(p -> Objects.equals(p.getMemberId(), memberId));
	}

	public ChatRoomInfoRes getChatRoomInfo(Long roomId, Long memberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		if (!isRoomParticipant(memberId, roomId))
			throw new IllegalArgumentException("Not a participant");

		List<String> nicknames = chatParticipantRepository.findByChatRoom(room)
			.stream().map(ChatParticipant::getNickname).toList();
		return new ChatRoomInfoRes(room.getRoomId(), room.getHostId(), room.getHostNickname(),
			room.getStartPoint(), room.getEndPoint(), room.getDepartAt(), room.getStatus(), nicknames);
	}
}
