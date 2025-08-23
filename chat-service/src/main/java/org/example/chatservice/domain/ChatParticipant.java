package org.example.chatservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatParticipant {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(nullable = false)
	private Long memberId;

	@Column(length = 30)
	private String nickname;

	private boolean isReady;

	@Column(nullable = false)
	private boolean isHost;

	public void setReady(boolean ready) { this.isReady = ready; }
}