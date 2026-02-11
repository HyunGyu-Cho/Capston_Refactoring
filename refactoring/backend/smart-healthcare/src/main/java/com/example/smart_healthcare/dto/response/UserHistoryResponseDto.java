package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.UserHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserHistoryResponseDto {
    private Long id;
    private Long userId;
    private LocalDate date;
    private String type; // "workout" or "diet"
    private String payload; // JSON 문자열
    private Boolean completed;
    
    public static UserHistoryResponseDto toDto(UserHistory history) {
        return new UserHistoryResponseDto(
            history.getId(),
            history.getUserId(),
            history.getDate(),
            history.getType(),
            history.getPayload(),
            history.getCompleted()
        );
    }
}