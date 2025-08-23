package org.example.authservice.auth.repository;

import java.util.Optional;

import org.example.authservice.auth.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);
}
