package org.example.chatservice;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

	@CreationTimestamp
	@Column(name = "created_time", updatable = false, nullable = false)
	private LocalDateTime createdTime;

	@UpdateTimestamp
	@Column(name = "updated_time", nullable = false)
	private LocalDateTime updatedTime;
}