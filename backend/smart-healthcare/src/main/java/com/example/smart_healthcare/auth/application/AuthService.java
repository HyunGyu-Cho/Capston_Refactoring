package com.example.smart_healthcare.auth.application;

import com.example.smart_healthcare.auth.dto.request.LoginRequest;
import com.example.smart_healthcare.auth.dto.request.SignupRequest;
import com.example.smart_healthcare.auth.dto.response.LoginResponse;
import com.example.smart_healthcare.auth.dto.response.MeResponse;
import com.example.smart_healthcare.auth.dto.response.SignupResponse;
import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.auth.security.JwtProvider;
import com.example.smart_healthcare.auth.security.MemberUserDetails;
import com.example.smart_healthcare.auth.security.TokenPair;
import com.example.smart_healthcare.auth.session.RefreshSession;
import com.example.smart_healthcare.auth.session.RefreshSessionRepository;
import com.example.smart_healthcare.auth.session.TokenHashUtil;
import com.example.smart_healthcare.common.error.AppException;
import com.example.smart_healthcare.member.domain.Member;
import com.example.smart_healthcare.member.domain.MemberRole;
import com.example.smart_healthcare.member.domain.Role;
import com.example.smart_healthcare.member.infrastructure.MemberRepository;
import com.example.smart_healthcare.member.infrastructure.MemberRoleRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshSessionRepository refreshSessionRepository;
    private final TokenHashUtil tokenHashUtil;

    public AuthService(
            MemberRepository memberRepository,
            MemberRoleRepository memberRoleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtProvider jwtProvider,
            RefreshSessionRepository refreshSessionRepository,
            TokenHashUtil tokenHashUtil
    ) {
        this.memberRepository = memberRepository;
        this.memberRoleRepository = memberRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.refreshSessionRepository = refreshSessionRepository;
        this.tokenHashUtil = tokenHashUtil;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AppException(AuthErrorCode.AUTH_409_001);
        }
        Member member = new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.gender(),
                request.birthDate()
        );
        memberRepository.save(member);
        memberRoleRepository.save(new MemberRole(member, Role.USER));

        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname(), "success");
    }

    public AuthResult login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.AUTH_401_001);
        }

        MemberUserDetails user = (MemberUserDetails) authentication.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .toList();

        TokenPair tokenPair = jwtProvider.issueTokens(user.getMemberId(), user.getUsername(), roles);
        saveRefreshSession(tokenPair.refreshToken());

        LoginResponse response = new LoginResponse(tokenPair.accessToken(), jwtProvider.accessExpireSec(), "Bearer");
        return new AuthResult(response, tokenPair.refreshToken());
    }

    public AuthResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(AuthErrorCode.AUTH_401_004);
        }

        Claims refreshClaims = jwtProvider.parseRefreshClaims(refreshToken);
        String jti = refreshClaims.get("jti", String.class);
        Long memberId = Long.valueOf(refreshClaims.getSubject());

        RefreshSession session = refreshSessionRepository.findByJti(jti)
                .orElseThrow(() -> new AppException(AuthErrorCode.AUTH_401_005));

        String currentHash = tokenHashUtil.sha256(refreshToken);
        if (session.isRevoked()) {
            if ("REUSE_DETECTED".equals(session.getRevokeReason())) {
                revokeAllMemberSessions(memberId);
                throw new AppException(AuthErrorCode.AUTH_401_008);
            }
            throw new AppException(AuthErrorCode.AUTH_401_007);
        }
        if (!session.getTokenHash().equals(currentHash)) {
            throw new AppException(AuthErrorCode.AUTH_401_005);
        }

        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(AuthErrorCode.AUTH_401_005));
        var roles = memberRoleRepository.findByMember(member).stream()
                .map(MemberRole::getRole)
                .map(Enum::name)
                .toList();

        TokenPair pair = jwtProvider.issueTokens(member.getId(), member.getEmail(), roles);
        Claims newRefreshClaims = jwtProvider.parseRefreshClaims(pair.refreshToken());
        String newJti = newRefreshClaims.get("jti", String.class);

        refreshSessionRepository.revoke(jti, "ROTATED", newJti);
        saveRefreshSession(pair.refreshToken());

        return new AuthResult(new LoginResponse(pair.accessToken(), jwtProvider.accessExpireSec(), "Bearer"), pair.refreshToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        Claims claims = jwtProvider.parseRefreshClaims(refreshToken);
        String jti = claims.get("jti", String.class);
        refreshSessionRepository.revoke(jti, "LOGOUT", null);
    }

    public MeResponse me(MemberUserDetails user) {
        List<String> roles = user.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .toList();
        return new MeResponse(user.getMemberId(), user.getUsername(), roles);
    }

    private void saveRefreshSession(String refreshToken) {
        Claims claims = jwtProvider.parseRefreshClaims(refreshToken);
        String jti = claims.get("jti", String.class);
        Long memberId = Long.valueOf(claims.getSubject());
        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiresAt = claims.getExpiration().toInstant();
        String tokenHash = tokenHashUtil.sha256(refreshToken);

        refreshSessionRepository.save(jti, new RefreshSession(memberId, tokenHash, issuedAt, expiresAt));
    }

    private void revokeAllMemberSessions(Long memberId) {
        for (String jti : refreshSessionRepository.findJtiByMemberId(memberId)) {
            refreshSessionRepository.revoke(jti, "REUSE_DETECTED", null);
        }
    }

    public record AuthResult(
            LoginResponse response,
            String refreshToken
    ) {
    }
}
