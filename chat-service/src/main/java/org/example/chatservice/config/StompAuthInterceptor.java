package org.example.chatservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.chatservice.service.ChatRoomService;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

	private final JwtDecoder jwtDecoder;
	private final ChatRoomService chatRoomService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT == accessor.getCommand() || StompCommand.SUBSCRIBE == accessor.getCommand()) {
			String token = extractToken(accessor);
			Jwt jwt = jwtDecoder.decode(token);
			Long memberId = Long.valueOf(jwt.getSubject());

			if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
				String dest = accessor.getDestination(); // e.g. /topic/123  or /topic/ready/123
				if (dest == null || !dest.startsWith("/topic/")) {
					throw new AuthenticationServiceException("Invalid destination");
				}
				String[] parts = dest.split("/");
				String last = parts[parts.length - 1];
				if (last.matches("\\d+")) {
					Long roomId = Long.valueOf(last);
					if (!chatRoomService.isRoomParticipant(memberId, roomId)) {
						throw new AuthenticationServiceException("No permission for room " + roomId);
					}
				}
			}
		}
		return message;
	}

	private String extractToken(StompHeaderAccessor accessor) {
		String bearer = accessor.getFirstNativeHeader("Authorization");
		if (bearer == null || !bearer.startsWith("Bearer ")) {
			throw new AuthenticationServiceException("Missing Authorization");
		}
		return bearer.substring(7).trim();
	}
}