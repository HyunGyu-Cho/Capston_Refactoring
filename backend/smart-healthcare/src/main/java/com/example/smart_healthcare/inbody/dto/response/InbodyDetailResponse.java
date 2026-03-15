package com.example.smart_healthcare.inbody.dto.response;

import java.time.LocalDateTime;

public record InbodyDetailResponse(
        Long inbodyId,
        LocalDateTime measuredAt,
        InbodyMemberSummary member,
        InbodyMetricsSummary metrics,
        String calculationVersion
) {
}
