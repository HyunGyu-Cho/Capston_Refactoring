package com.example.smart_healthcare.member.infrastructure;

import com.example.smart_healthcare.member.domain.Member;
import com.example.smart_healthcare.member.domain.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {
    List<MemberRole> findByMember(Member member);
}
