package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.auth.error.AuthErrorCode;
import com.example.smart_healthcare.common.error.AppException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    @Value("${app.auth.jwt.secret}")
    private String secret;

    @Value("${app.auth.jwt.issuer}")
    private String issuer;

    @Value("${app.auth.jwt.access-expire-sec}")
    private long accessExpireSec;

    @Value("${app.auth.jwt.refresh-expire-sec}")
    private long refreshExpireSec;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair issueTokens(Long memberId, String email, List<String> roles) {
        String access = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(issuer)
                .issuedAt(now())
                .expiration(exp(accessExpireSec))
                .claim("email", email)
                .claim("roles", roles)
                .claim(TOKEN_TYPE, ACCESS)
                .signWith(secretKey)
                .compact();

        String refresh = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(issuer)
                .issuedAt(now())
                .expiration(exp(refreshExpireSec))
                .claim("jti", UUID.randomUUID().toString())
                .claim(TOKEN_TYPE, REFRESH)
                .signWith(secretKey)
                .compact();

        return new TokenPair(access, refresh);
    }

    public Claims parseAccessClaims(String token) {
        return parse(token, ACCESS, AuthErrorCode.AUTH_401_002, AuthErrorCode.AUTH_401_003);
    }

    public Claims parseRefreshClaims(String token) {
        return parse(token, REFRESH, AuthErrorCode.AUTH_401_005, AuthErrorCode.AUTH_401_006);
    }

    private Claims parse(String token, String expectedType, AuthErrorCode invalidCode, AuthErrorCode expiredCode) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.equals(claims.get(TOKEN_TYPE, String.class))) {
                throw new AppException(invalidCode);
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new AppException(expiredCode);
        } catch (AppException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(invalidCode);
        }
    }

    private Date now() {
        return Date.from(Instant.now());
    }

    private Date exp(long sec) {
        return Date.from(Instant.now().plusSeconds(sec));
    }

    public long accessExpireSec() {
        return accessExpireSec;
    }

    public long refreshExpireSec() {
        return refreshExpireSec;
    }
}
