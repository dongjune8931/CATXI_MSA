package org.example.chatservice.controller;


import lombok.RequiredArgsConstructor;

import org.example.chatservice.dto.ChatMessageRes;
import org.example.chatservice.dto.ChatRoomInfoRes;
import org.example.chatservice.dto.KickRequest;
import org.example.chatservice.dto.RoomCreateReq;
import org.example.chatservice.dto.RoomCreateRes;
import org.example.chatservice.service.ChatMessageService;
import org.example.chatservice.service.ChatRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatMessageService chatMessageService;
	private final ChatRoomService chatRoomService;

	@PostMapping("/room/create")
	public ResponseEntity<RoomCreateRes> createRoom(@RequestBody RoomCreateReq req,
		@AuthenticationPrincipal Jwt jwt) {
		Long memberId = Long.valueOf(jwt.getSubject());
		String nickname = (String) jwt.getClaims().getOrDefault("nickname", "USER");
		return ResponseEntity.ok(chatRoomService.createRoom(req, memberId, nickname));
	}

	@GetMapping("/{roomId}/messages")
	public ResponseEntity<List<ChatMessageRes>> history(@PathVariable Long roomId,
		@AuthenticationPrincipal Jwt jwt) {
		Long memberId = Long.valueOf(jwt.getSubject());
		return ResponseEntity.ok(chatMessageService.getChatHistory(roomId, memberId));
	}

	@DeleteMapping("/{roomId}/leave")
	public ResponseEntity<Void> leave(@PathVariable Long roomId,
		@AuthenticationPrincipal Jwt jwt) {
		chatRoomService.leaveChatRoom(roomId, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/rooms/{roomId}/join")
	public ResponseEntity<Void> join(@PathVariable Long roomId,
		@AuthenticationPrincipal Jwt jwt) {
		Long memberId = Long.valueOf(jwt.getSubject());
		String nickname = (String) jwt.getClaims().getOrDefault("nickname", "USER");
		chatRoomService.joinChatRoom(roomId, memberId, nickname);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/rooms/{roomId}/kick")
	public ResponseEntity<Void> kick(@PathVariable Long roomId,
		@RequestBody KickRequest req,
		@AuthenticationPrincipal Jwt jwt) {
		chatRoomService.kickUser(roomId, Long.valueOf(jwt.getSubject()), req.targetMemberId());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/rooms/myid")
	public ResponseEntity<Long> myRoom(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(chatRoomService.getMyChatRoomId(Long.valueOf(jwt.getSubject())));
	}

	@GetMapping("/rooms/{roomId}")
	public ResponseEntity<ChatRoomInfoRes> info(@PathVariable Long roomId,
		@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity.ok(
			chatRoomService.getChatRoomInfo(roomId, Long.valueOf(jwt.getSubject())));
	}
}
