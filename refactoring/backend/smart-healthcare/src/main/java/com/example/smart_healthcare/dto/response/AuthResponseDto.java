package com.example.smart_healthcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    
    private String token;
    private UserResponseDto user;
    
    // 회원가입 응답용
    public static AuthResponseDto signupSuccess(String token, UserResponseDto user) {
        return new AuthResponseDto(token, user);
    }
    
    // 로그인 응답용
    public static AuthResponseDto loginSuccess(String token, UserResponseDto user) {
        return new AuthResponseDto(token, user);
    }
}
