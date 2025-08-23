package org.example.authservice.auth.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//이름
	@Column(nullable = false, length = 30)
	private String membername;

	//닉네임
	@Column(nullable = true, length = 30)
	private String nickname;

	//이메일
	@Column(nullable = false,length = 30)
	private String email;

	//학번
	@Column(nullable = true, length = 20, unique = true)
	private Long studentNo;

	private String role;

	//조회 기록
	@Column(nullable = false)
	private int matchCount;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private boolean isLogin;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberStatus status;

	// 로그인했음 표시
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

	// 삭제 필드
	public void delete() {
		this.status = MemberStatus.INACTIVE;
	}


}
