package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.common.error.AppException;
import com.example.smart_healthcare.member.infrastructure.MemberRepository;
import com.example.smart_healthcare.member.infrastructure.MemberRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

    public MemberUserDetailsService(MemberRepository memberRepository, MemberRoleRepository memberRoleRepository) {
        this.memberRepository = memberRepository;
        this.memberRoleRepository = memberRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(AuthErrorCode.AUTH_401_001));

        var authorities = memberRoleRepository.findByMember(member).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().name()))
                .collect(Collectors.toList());

        return new MemberUserDetails(member.getId(), member.getEmail(), member.getPasswordHash(), authorities);
    }
}
