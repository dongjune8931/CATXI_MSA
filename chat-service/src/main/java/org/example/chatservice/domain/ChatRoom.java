package org.example.chatservice.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.chatservice.domain.ENUM.Location;
import org.example.chatservice.domain.ENUM.RoomStatus;

@Getter @Entity @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roomId;

	// 호스트 정보 (Member 직접참조 X)
	@Column(nullable = false)
	private Long hostId;

	@Column(nullable = true, length = 30)
	private String hostNickname;

	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private Location startPoint;

	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private Location endPoint;

	@Column(nullable = false)
	private LocalDateTime departAt;

	@Enumerated(EnumType.STRING) @Column(nullable = false)
	@Setter
	private RoomStatus status;

	@Column(nullable = false)
	private Long maxCapacity;

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<ChatParticipant> participants = new ArrayList<>();

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<KickedParticipant> kickedParticipants = new ArrayList<>();

	@Column
	private LocalDateTime matchedAt;

	public void matchedStatus(RoomStatus newStatus) {
		if (status == RoomStatus.READY_LOCKED && this.matchedAt == null) {
			this.matchedAt = LocalDateTime.now();
		}
		this.status = newStatus;
	}
}
