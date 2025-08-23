package org.example.authservice.auth.controller;

import java.util.Map;

import org.example.authservice.auth.repository.MemberRepository;
import org.example.authservice.config.RefreshTokenStore;
import org.example.authservice.auth.domain.Member;
import org.example.authservice.jwt.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth-service/auth")
@RequiredArgsConstructor
public class AuthController {

	private final JwtProvider jwtProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final MemberRepository memberRepository;

	@GetMapping("/status")
	public ResponseEntity<?> status(Authentication authentication) {
		boolean authenticated = authentication != null && authentication.isAuthenticated();
		return ResponseEntity.ok(
			Map.of("authenticated", authenticated, "principal", authentication != null ? authentication.getName() : null));
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(Authentication authentication) {
		if (authentication == null) return ResponseEntity.status(401).build();
		Long memberId = Long.parseLong(authentication.getName());
		Member member = memberRepository.findById(memberId).orElse(null);
		if (member == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(Map.of(
			"memberId", member.getId(),
			"email", member.getEmail(),
			"nickname", member.getNickname(),
			"role", member.getRole()
		));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
		Long memberId = req.memberId();
		String stored = refreshTokenStore.get(memberId);
		if (stored == null || !stored.equals(req.refreshToken())) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
		}
		Member member = memberRepository.findById(memberId).orElse(null);
		if (member == null) return ResponseEntity.status(401).body(Map.of("error","Member not found"));

		String newAccess = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
		String newRefresh = jwtProvider.createRefreshToken(member.getId());
		refreshTokenStore.save(member.getId(), newRefresh);

		return ResponseEntity.ok(Map.of(
			"accessToken", newAccess,
			"refreshToken", newRefresh
		));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestBody LogoutRequest req, Authentication authentication) {
		refreshTokenStore.delete(req.memberId());
		return ResponseEntity.ok(Map.of("ok", true));
	}

	public record RefreshRequest(Long memberId, String refreshToken) {}
	public record LogoutRequest(Long memberId) {}
}