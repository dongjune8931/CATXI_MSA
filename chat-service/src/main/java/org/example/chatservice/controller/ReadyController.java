package org.example.chatservice.controller;

import org.example.chatservice.service.ReadyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ready")
@RequiredArgsConstructor
public class ReadyController {

	private final ReadyService readyService;

	@PostMapping("/request/{roomId}")
	public ResponseEntity<Void> request(@PathVariable Long roomId, @AuthenticationPrincipal Jwt jwt) {
		readyService.requestReady(roomId, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/accept/{roomId}")
	public ResponseEntity<Void> accept(@PathVariable Long roomId, @AuthenticationPrincipal Jwt jwt) {
		readyService.acceptReady(roomId, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/reject/{roomId}")
	public ResponseEntity<Void> reject(@PathVariable Long roomId, @AuthenticationPrincipal Jwt jwt) {
		readyService.rejectReady(roomId, Long.valueOf(jwt.getSubject()));
		return ResponseEntity.ok().build();
	}
}
