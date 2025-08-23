package org.example.authservice.auth.service;

import java.util.Map;
import java.util.Optional;

import org.example.authservice.auth.domain.Member;
import org.example.authservice.auth.domain.MemberStatus;
import org.example.authservice.auth.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		if (!"kakao".equals(userRequest.getClientRegistration().getRegistrationId())) {
			return oAuth2User;
		}

		Map<String, Object> attributes = oAuth2User.getAttributes();
		Long kakaoId = ((Number) attributes.get("id")).longValue();

		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
		Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
		String nickname = profile != null ? (String) profile.get("nickname") : null;

		// upsert member
		Optional<Member> existing = memberRepository.findByEmail(email);
		Member member = existing.orElseGet(() -> Member.builder()
			.email(email != null ? email : ("kakao_" + kakaoId))
			.membername(nickname != null ? nickname : "KakaoUser")
			.nickname(nickname)
			.role("USER")
			.studentNo(null)
			.matchCount(0)
			.password("{noop}") // 소셜 로그인만 사용
			.isLogin(true)
			.status(MemberStatus.PENDING)
			.build()
		);
		member.setLogin(true);
		if (nickname != null) member.setNickname(nickname);
		memberRepository.save(member);

		return oAuth2User;
	}
}