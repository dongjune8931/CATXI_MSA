package org.example.chatservice.service;


import lombok.RequiredArgsConstructor;

import org.example.chatservice.domain.ChatParticipant;
import org.example.chatservice.domain.ChatRoom;
import org.example.chatservice.domain.ENUM.RoomStatus;
import org.example.chatservice.dto.ReadyMessageEvent;
import org.example.chatservice.dto.ReadyMessageRes;
import org.example.chatservice.repository.ChatParticipantRepository;
import org.example.chatservice.repository.ChatRoomRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadyService {

	private final ApplicationEventPublisher eventPublisher;
	private final ChatParticipantRepository chatParticipantRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final TimerService timerService;
	private final ChatRoomService chatRoomService;

	/** 방장만 호출: 레디 세션 시작 */
	@Transactional
	public void requestReady(Long roomId, Long requesterId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));

		if (!requesterId.equals(room.getHostId())) {
			throw new IllegalArgumentException("Only host can start ready.");
		}
		if (!RoomStatus.WAITING.equals(room.getStatus())) {
			throw new IllegalArgumentException("Room not in WAITING status.");
		}

		room.setStatus(RoomStatus.READY_LOCKED); // 잠금 시작

		// 이벤트 발행 (AFTER_COMMIT에 Redis로 브로드캐스트)
		eventPublisher.publishEvent(
			new ReadyMessageEvent("ready:" + roomId,
				ReadyMessageRes.readyRequest(roomId, room.getHostId(), room.getHostNickname()))
		);

		// 타임아웃 체크 스케줄
		timerService.scheduleReadyTimeout(String.valueOf(roomId));
	}

	/** 참여자: 수락 */
	@Transactional
	public void acceptReady(Long roomId, Long memberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		ChatParticipant participant = chatParticipantRepository.findByChatRoomAndMemberId(room, memberId)
			.orElseThrow(() -> new IllegalArgumentException("Not a participant"));

		validateDuringReady(room, participant);

		participant.setReady(true); // 수락 표시

		eventPublisher.publishEvent(
			new ReadyMessageEvent("ready:" + roomId,
				ReadyMessageRes.readyAccept(roomId, memberId, participant.getNickname()))
		);
	}

	/** 참여자: 거절 (기존 동작과 동일하게 방에서 내보냄) */
	@Transactional
	public void rejectReady(Long roomId, Long memberId) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("Room not found"));
		ChatParticipant participant = chatParticipantRepository.findByChatRoomAndMemberId(room, memberId)
			.orElseThrow(() -> new IllegalArgumentException("Not a participant"));

		validateDuringReady(room, participant);

		eventPublisher.publishEvent(
			new ReadyMessageEvent("ready:" + roomId,
				ReadyMessageRes.readyDeny(roomId, memberId, participant.getNickname()))
		);

		// 거절 시 방에서 나가게 하는 기존 정책 유지
		chatRoomService.leaveChatRoom(roomId, memberId);
	}

	/** READY_LOCKED 상태에서만, 호스트는 제외, 중복 수락 방지 */
	private void validateDuringReady(ChatRoom room, ChatParticipant participant) {
		if (!RoomStatus.READY_LOCKED.equals(room.getStatus())) {
			throw new IllegalArgumentException("Room not in READY_LOCKED.");
		}
		if (participant.isHost()) {
			throw new IllegalArgumentException("Host cannot accept/reject.");
		}
		if (participant.isReady()) {
			throw new IllegalArgumentException("Already accepted.");
		}
	}
}
