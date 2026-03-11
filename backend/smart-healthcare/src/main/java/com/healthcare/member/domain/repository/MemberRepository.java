package com.healthcare.member.domain.repository;

import com.healthcare.member.domain.entity.Member;

import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmail(String email);

    Member save(Member member);
}
