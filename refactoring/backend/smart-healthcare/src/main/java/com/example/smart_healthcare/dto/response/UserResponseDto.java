package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private User.Role role;
    private LocalDateTime createdAt;
    
    // 비밀번호는 응답에서 제외 (보안)
    
    public static UserResponseDto toDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}