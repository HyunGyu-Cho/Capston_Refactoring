package com.example.smart_healthcare.auth.session;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
// refresh 토큰 값을 단방향 해시로 변환하는 유틸리티.
// 세션 저장소에 토큰 원문을 직접 저장하지 않도록 한다.
public class TokenHashUtil {

    // 입력 문자열에 대한 SHA-256 16진수 해시를 반환한다.
    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

