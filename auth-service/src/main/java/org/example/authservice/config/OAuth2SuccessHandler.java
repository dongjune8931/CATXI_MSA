package org.example.authservice.config;

import java.io.IOException;
import java.util.Map;

import org.example.authservice.auth.domain.Member;
import org.example.authservice.auth.repository.MemberRepository;
import org.example.authservice.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final MemberRepository memberRepository;
	private final RefreshTokenStore refreshTokenStore;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
		Map<String, Object> kakaoAccount = (Map<String, Object>) principal.getAttributes().get("kakao_account");
		String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalStateException("Member not found after OAuth2 login"));

		String access = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
		String refresh = jwtProvider.createRefreshToken(member.getId());

		// Refresh 저장 (memberId -> refresh) + TTL
		refreshTokenStore.save(member.getId(), refresh);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), Map.of(
			"memberId", member.getId(),
			"email", member.getEmail(),
			"role", member.getRole(),
			"accessToken", access,
			"refreshToken", refresh
		));
	}
}
