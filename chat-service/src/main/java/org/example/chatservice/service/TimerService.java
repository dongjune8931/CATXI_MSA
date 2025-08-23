package org.example.chatservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.RoomStatus;
import org.example.chatservice.repository.ChatParticipantRepository;
import org.example.chatservice.repository.ChatRoomRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerService {

	private final StringRedisTemplate redis;
	private final TaskScheduler taskScheduler;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatParticipantRepository chatParticipantRepository;

	private String key(String roomId) { return "ready:" + roomId; }

	/**
	 * 레디 요청 시점의 '참여자 수'를 Redis에 TTL로 저장하고,
	 * 일정 시간 뒤 상태 점검 작업을 예약합니다.
	 */
	public void scheduleReadyTimeout(String roomId) {
		Long id = Long.valueOf(roomId);
		ChatRoom room = chatRoomRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

		long participantCount = chatParticipantRepository.countByChatRoom(room);

		// 25초 TTL로 저장
		redis.opsForValue().set(key(roomId), String.valueOf(participantCount), Duration.ofSeconds(25));

		// 20초 뒤 점검 스케줄
		taskScheduler.schedule(() -> {
			try {
				checkAndUpdateRoomStatus(roomId);
			} catch (Exception e) {
				log.error("Ready timeout check failed for roomId={}", roomId, e);
			}
		}, Instant.now().plusSeconds(20));
	}

	/**
	 * READY_LOCKED 상태에서,
	 * - 레디 요청 당시 인원 수 == 현재 'isReady=true' 인원 수 → MATCHED
	 * - 아니면 미준비 인원을 정리/리셋하고 WAITING으로 복귀
	 */
	@Transactional
	public void checkAndUpdateRoomStatus(String roomId) {
		Long id = Long.valueOf(roomId);
		ChatRoom room = chatRoomRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

		if (room.getStatus() != RoomStatus.READY_LOCKED) {
			// 이미 다른 상태로 바뀌었으면 스킵
			redis.delete(key(roomId));
			return;
		}

		String savedCountStr = redis.opsForValue().get(key(roomId));

		if (savedCountStr == null) {
			// 키 만료/유실 등 → 전원 레디 해제(호스트 제외), 상태 되돌리기
			chatParticipantRepository.updateIsReadyFalseExceptHost(id);
			room.setStatus(RoomStatus.WAITING);
			chatRoomRepository.save(room);
			return;
		}

		long savedCount = Long.parseLong(savedCountStr);
		long readyCount = chatParticipantRepository.countByChatRoomAndIsReady(room, true);

		if (savedCount == readyCount) {
			room.matchedStatus(RoomStatus.MATCHED);
		} else {
			// 미준비 인원 제거(호스트 제외), 나머지 레디 플래그 해제, 상태 복귀
			chatParticipantRepository.deleteAllNotReadyExceptHost(room);
			chatParticipantRepository.updateIsReadyFalseExceptHost(id);
			room.setStatus(RoomStatus.WAITING);
		}

		chatRoomRepository.save(room);
		redis.delete(key(roomId));
	}
}
