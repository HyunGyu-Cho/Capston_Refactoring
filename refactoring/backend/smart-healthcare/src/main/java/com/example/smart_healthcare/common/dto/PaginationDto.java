package com.example.smart_healthcare.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationDto {
    
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // 생성자
    public PaginationDto(int currentPage, int totalPages, long totalElements, int size) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
    
    // 정적 팩토리 메서드
    public static PaginationDto of(int currentPage, int totalPages, long totalElements, int size) {
        return new PaginationDto(currentPage, totalPages, totalElements, size);
    }
}
