package com.healthcare.member.infrastructure.persistence;

import com.healthcare.member.domain.entity.Member;
import com.healthcare.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryImpl(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}
