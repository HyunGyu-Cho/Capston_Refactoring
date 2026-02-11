package com.example.smart_healthcare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    private String password;
    
}
