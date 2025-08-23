package org.example.chatservice.domain;

import org.example.chatservice.BaseTimeEntity;
import org.example.chatservice.domain.ENUM.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private MessageType msgType;

	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(nullable = true)
	private Long senderId;

	@Column(length = 30)
	private String senderNickname;

	@Column(nullable = false, length = 500)
	private String content;
}