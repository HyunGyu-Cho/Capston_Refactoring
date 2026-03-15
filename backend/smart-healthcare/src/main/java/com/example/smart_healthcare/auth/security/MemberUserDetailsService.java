package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.common.error.AppException;
import com.example.smart_healthcare.member.infrastructure.MemberRepository;
import com.example.smart_healthcare.member.infrastructure.MemberRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//UserDetailsService 인터페이스 구현체
//Spring Security가 사용자 정보를 조회하는 서비스
//즉, 사용자 이메일을 받아서 사용자 정보를 조회하는 서비스
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

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


