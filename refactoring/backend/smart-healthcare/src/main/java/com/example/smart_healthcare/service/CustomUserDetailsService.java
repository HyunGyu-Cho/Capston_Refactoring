package com.example.smart_healthcare.service;

import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("사용자 인증 요청: email={}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: email={}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        log.info("사용자 인증 성공: email={}, role={}", user.getEmail(), user.getRole());
        
        return new CustomUserPrincipal(user);
    }

    /**
     * Spring Security UserDetails 구현체
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // ROLE_ 접두사를 추가하여 Spring Security 권한 형식에 맞춤
            return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return !user.getIsDeleted();
        }

        // User 엔티티에 직접 접근할 수 있는 메서드
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public User.Role getRole() {
            return user.getRole();
        }
    }
}
