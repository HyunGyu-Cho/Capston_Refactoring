package com.example.smart_healthcare.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberUserDetails implements UserDetails {
    private final Long memberId;
    private final String email;
    private final String passwordHash;
    private final List<? extends GrantedAuthority> authorities;

    public MemberUserDetails(Long memberId, String email, String passwordHash, List<? extends GrantedAuthority> authorities) {
        this.memberId = memberId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.authorities = authorities;
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
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
