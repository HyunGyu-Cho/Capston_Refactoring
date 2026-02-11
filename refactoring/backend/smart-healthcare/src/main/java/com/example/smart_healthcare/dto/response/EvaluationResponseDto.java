package com.example.smart_healthcare.dto.response;

import com.example.smart_healthcare.entity.Evaluation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 평가 응답 DTO
 * - 서버에서 클라이언트로 전송되는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponseDto {

    private Long id;
    private Long userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Evaluation 엔티티를 EvaluationResponseDto로 변환하는 팩토리 메서드
     * @param evaluation Evaluation 엔티티
     * @return EvaluationResponseDto
     */
    public static EvaluationResponseDto toDto(Evaluation evaluation) {
        return new EvaluationResponseDto(
            evaluation.getId(),
            evaluation.getUser().getId(),
            evaluation.getRating(),
            evaluation.getComment(),
            evaluation.getCreatedAt()
        );
    }
}
