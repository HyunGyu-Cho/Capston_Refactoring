package com.example.smart_healthcare.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
// 인증 완료 후 SecurityContext에 저장되는 Principal 모델.
public class MemberUserDetails implements UserDetails {
    @Getter
    private final Long memberId;
    private final String email;
    private final String passwordHash;
    private final List<? extends GrantedAuthority> authorities;

    @Override
    // 인가 판단에 사용되는 권한 목록.
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}


