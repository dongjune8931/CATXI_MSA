package org.example.authservice.config;

import java.io.IOException;
import java.util.List;

import org.example.authservice.auth.service.KakaoOAuth2UserService;
import org.example.authservice.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final KakaoOAuth2UserService kakaoOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final JwtProvider jwtProvider;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(reg -> reg
				.requestMatchers("/auth-service/health").permitAll()
				.requestMatchers("/auth-service/oauth2/**", "/auth-service/login/**").permitAll()
				.requestMatchers("/auth-service/auth/refresh").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth -> oauth
				.userInfoEndpoint(ui -> ui.userService(kakaoOAuth2UserService))
				.successHandler(oAuth2SuccessHandler)
			)
			.addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	static class JwtAuthFilter extends OncePerRequestFilter {
		private final JwtProvider jwtProvider;
		JwtAuthFilter(JwtProvider jwtProvider) { this.jwtProvider = jwtProvider; }

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
			String auth = request.getHeader("Authorization");
			if (auth != null && auth.startsWith("Bearer ")) {
				String token = auth.substring(7);
				try {
					var jws = jwtProvider.parse(token);
					String sub = jws.getBody().getSubject(); // memberId
					String role = (String) jws.getBody().get("role");

					Authentication authentication = new UsernamePasswordAuthenticationToken(
						sub,
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + role))
					);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} catch (Exception ignored) { /* 무시하고 익명 처리 */ }
			}
			chain.doFilter(request, response);
		}
	}
}