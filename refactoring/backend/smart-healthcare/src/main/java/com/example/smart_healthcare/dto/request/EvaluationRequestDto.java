package com.example.smart_healthcare.dto.request;

import com.example.smart_healthcare.entity.Evaluation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 평가 요청 DTO
 * - 클라이언트에서 서버로 전송되는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequestDto {

    private Long userId; // 선택적 필드로 변경 (프론트엔드에서 전송하지 않을 수 있음)

    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다")
    private Integer rating;

    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    private String comment;

    /**
     * EvaluationRequestDto를 Evaluation 엔티티로 변환하는 메서드
     * @return Evaluation 엔티티
     */
    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();
        evaluation.setRating(this.rating);
        evaluation.setComment(this.comment);
        return evaluation;
    }
}
