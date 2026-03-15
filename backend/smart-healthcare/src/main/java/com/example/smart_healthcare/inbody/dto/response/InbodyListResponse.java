package com.example.smart_healthcare.inbody.dto.response;

import java.util.List;

public record InbodyListResponse(
        List<InbodySummaryItem> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
