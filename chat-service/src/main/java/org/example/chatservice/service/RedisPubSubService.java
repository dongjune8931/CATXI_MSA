package org.example.chatservice.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

import org.example.chatservice.dto.ChatMessageSendReq;
import org.example.chatservice.dto.ParticipantsUpdateMessage;
import org.example.chatservice.dto.ReadyMessageRes;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPubSubService implements MessageListener {


	private final SimpMessagingTemplate messaging;   // ★
	private final StringRedisTemplate redis;         // ★
	private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

	public void publish(String channel, String payload) {
		redis.convertAndSend(channel, payload);
	}

	@Override
	public void onMessage(Message msg, byte[] pattern) {
		String channel = new String(msg.getChannel()); // ★ 이것이 실제 채널
		String payload = new String(msg.getBody());
		try {
			if ("chat".equals(channel)) {
				ChatMessageSendReq dto = om.readValue(payload, ChatMessageSendReq.class);
				messaging.convertAndSend("/topic/" + dto.roomId(), dto);
			} else if (channel.startsWith("ready:")) {
				ReadyMessageRes dto = om.readValue(payload, ReadyMessageRes.class);
				messaging.convertAndSend("/topic/ready/" + dto.roomId(), dto);
			} else if (channel.startsWith("participants:")) {
				ParticipantsUpdateMessage dto = om.readValue(payload, ParticipantsUpdateMessage.class);
				messaging.convertAndSend("/topic/room/" + dto.roomId() + "/participants", dto);
			} else if (channel.startsWith("kick:")) {
				String memberId = channel.split(":")[1];
				messaging.convertAndSendToUser(memberId, "/queue/kick", "KICKED");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

