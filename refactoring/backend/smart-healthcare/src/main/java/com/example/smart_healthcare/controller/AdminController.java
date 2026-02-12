package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.entity.Member;
import com.example.smart_healthcare.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 전용 API 컨트롤러
 * - 사용자 관리
 * - 시스템 통계
 * - 데이터 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 대시보드 통계 조회
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getDashboardStats() {
        log.info("🔧 관리자 대시보드 통계 조회 요청");
        
        try {
            Map<String, Object> stats = adminService.getDashboardStatistics();
            return ResponseEntity.ok(ApiResponseDto.success("관리자 대시보드 통계 조회 완료", stats));
        } catch (Exception e) {
            log.error("관리자 대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("관리자 대시보드 통계 조회에 실패했습니다."));
        }
    }

    /**
     * 전체 사용자 목록 조회 (페이징)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDto<Page<Member>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        log.info("👥 전체 사용자 목록 조회 - 페이지: {}, 검색: {}", pageable.getPageNumber(), search);
        
        try {
            Page<Member> users = adminService.getAllUsers(pageable, search);
            return ResponseEntity.ok(ApiResponseDto.success("사용자 목록 조회 완료", users));
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("사용자 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 사용자 역할 변경
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponseDto<Member>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Member.Role role) {
        log.info("🔄 사용자 역할 변경 - userId: {}, newRole: {}", userId, role);
        
        try {
            Member updatedUser = adminService.updateUserRole(userId, role);
            return ResponseEntity.ok(ApiResponseDto.success("사용자 역할이 변경되었습니다.", updatedUser));
        } catch (Exception e) {
            log.error("사용자 역할 변경 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("사용자 역할 변경에 실패했습니다."));
        }
    }

    /**
     * 사용자 계정 활성화/비활성화
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponseDto<Member>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean isDeleted) {
        log.info("🔄 사용자 상태 변경 - userId: {}, isDeleted: {}", userId, isDeleted);
        
        try {
            Member updatedUser = adminService.updateUserStatus(userId, isDeleted);
            String message = isDeleted ? "사용자가 비활성화되었습니다." : "사용자가 활성화되었습니다.";
            return ResponseEntity.ok(ApiResponseDto.success(message, updatedUser));
        } catch (Exception e) {
            log.error("사용자 상태 변경 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("사용자 상태 변경에 실패했습니다."));
        }
    }

    /**
     * 시스템 전체 데이터 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getSystemStatistics() {
        log.info("📊 시스템 전체 데이터 통계 조회");
        
        try {
            Map<String, Object> statistics = adminService.getSystemStatistics();
            return ResponseEntity.ok(ApiResponseDto.success("시스템 통계 조회 완료", statistics));
        } catch (Exception e) {
            log.error("시스템 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("시스템 통계 조회에 실패했습니다."));
        }
    }

    /**
     * 최근 활동 로그 조회
     */
    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getActivityLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("📝 최근 활동 로그 조회");
        
        try {
            Page<Map<String, Object>> logs = adminService.getActivityLogs(pageable);
            return ResponseEntity.ok(ApiResponseDto.success("활동 로그 조회 완료", logs));
        } catch (Exception e) {
            log.error("활동 로그 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("활동 로그 조회에 실패했습니다."));
        }
    }

    /**
     * 데이터베이스 백업 트리거
     */
    @PostMapping("/backup")
    public ResponseEntity<ApiResponseDto<String>> triggerBackup() {
        log.info("💾 데이터베이스 백업 트리거");
        
        try {
            String backupResult = adminService.triggerDatabaseBackup();
            return ResponseEntity.ok(ApiResponseDto.success("백업이 시작되었습니다.", backupResult));
        } catch (Exception e) {
            log.error("데이터베이스 백업 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("백업 실행에 실패했습니다."));
        }
    }

    /**
     * 시스템 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getSystemHealth() {
        log.info("🏥 시스템 상태 체크");
        
        try {
            Map<String, Object> health = adminService.getSystemHealth();
            return ResponseEntity.ok(ApiResponseDto.success("시스템 상태 조회 완료", health));
        } catch (Exception e) {
            log.error("시스템 상태 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("시스템 상태 조회에 실패했습니다."));
        }
    }

    // ===== 커뮤니티 관리 API =====

    /**
     * 게시글 목록 조회 (관리자용)
     */
    @GetMapping("/community/posts")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getCommunityPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        log.info("📝 커뮤니티 게시글 목록 조회 - 검색: {}, 카테고리: {}, 상태: {}", search, category, status);
        
        try {
            Page<Map<String, Object>> posts = adminService.getCommunityPosts(pageable, search, category, status);
            return ResponseEntity.ok(ApiResponseDto.success("게시글 목록 조회 완료", posts));
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("게시글 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 댓글 목록 조회 (관리자용)
     */
    @GetMapping("/community/comments")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getCommunityComments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        log.info("💬 커뮤니티 댓글 목록 조회 - 검색: {}, 상태: {}", search, status);
        
        try {
            Page<Map<String, Object>> comments = adminService.getCommunityComments(pageable, search, status);
            return ResponseEntity.ok(ApiResponseDto.success("댓글 목록 조회 완료", comments));
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("댓글 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 게시글 삭제 (관리자용)
     */
    @DeleteMapping("/community/posts/{postId}")
    public ResponseEntity<ApiResponseDto<String>> deleteCommunityPost(@PathVariable Long postId) {
        log.info("🗑️ 게시글 삭제 - postId: {}", postId);
        
        try {
            adminService.deleteCommunityPost(postId);
            return ResponseEntity.ok(ApiResponseDto.success("게시글이 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("게시글 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("게시글 삭제에 실패했습니다."));
        }
    }

    /**
     * 댓글 삭제 (관리자용)
     */
    @DeleteMapping("/community/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<String>> deleteCommunityComment(@PathVariable Long commentId) {
        log.info("🗑️ 댓글 삭제 - commentId: {}", commentId);
        
        try {
            adminService.deleteCommunityComment(commentId);
            return ResponseEntity.ok(ApiResponseDto.success("댓글이 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("댓글 삭제에 실패했습니다."));
        }
    }

    /**
     * 게시글 복원 (관리자용)
     */
    @PutMapping("/community/posts/{postId}/restore")
    public ResponseEntity<ApiResponseDto<String>> restoreCommunityPost(@PathVariable Long postId) {
        log.info("♻️ 게시글 복원 - postId: {}", postId);
        
        try {
            adminService.restoreCommunityPost(postId);
            return ResponseEntity.ok(ApiResponseDto.success("게시글이 복원되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("게시글 복원 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("게시글 복원에 실패했습니다."));
        }
    }

    /**
     * 댓글 복원 (관리자용)
     */
    @PutMapping("/community/comments/{commentId}/restore")
    public ResponseEntity<ApiResponseDto<String>> restoreCommunityComment(@PathVariable Long commentId) {
        log.info("♻️ 댓글 복원 - commentId: {}", commentId);
        
        try {
            adminService.restoreCommunityComment(commentId);
            return ResponseEntity.ok(ApiResponseDto.success("댓글이 복원되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("댓글 복원 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("댓글 복원에 실패했습니다."));
        }
    }

    // ===== 콘텐츠 관리 API =====

    /**
     * 인바디 데이터 목록 조회
     */
    @GetMapping("/content/inbody")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getInbodyData(
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFilter) {
        log.info("📊 인바디 데이터 목록 조회 - 검색: {}, 날짜: {}", search, dateFilter);
        
        try {
            Page<Map<String, Object>> data = adminService.getInbodyData(pageable, search, dateFilter);
            return ResponseEntity.ok(ApiResponseDto.success("인바디 데이터 조회 완료", data));
        } catch (Exception e) {
            log.error("인바디 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("인바디 데이터 조회에 실패했습니다."));
        }
    }

    /**
     * 체형 분석 데이터 목록 조회
     */
    @GetMapping("/content/analysis")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getBodyAnalysisData(
            @PageableDefault(size = 20, sort = "analyzedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFilter) {
        log.info("🔍 체형 분석 데이터 목록 조회 - 검색: {}, 날짜: {}", search, dateFilter);
        
        try {
            Page<Map<String, Object>> data = adminService.getBodyAnalysisData(pageable, search, dateFilter);
            return ResponseEntity.ok(ApiResponseDto.success("체형 분석 데이터 조회 완료", data));
        } catch (Exception e) {
            log.error("체형 분석 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("체형 분석 데이터 조회에 실패했습니다."));
        }
    }

    /**
     * 운동 추천 데이터 목록 조회
     */
    @GetMapping("/content/workout-recommendations")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getWorkoutRecommendations(
            @PageableDefault(size = 20, sort = "recommendedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFilter) {
        log.info("🏃 운동 추천 데이터 목록 조회 - 검색: {}, 날짜: {}", search, dateFilter);
        
        try {
            Page<Map<String, Object>> data = adminService.getWorkoutRecommendations(pageable, search, dateFilter);
            return ResponseEntity.ok(ApiResponseDto.success("운동 추천 데이터 조회 완료", data));
        } catch (Exception e) {
            log.error("운동 추천 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("운동 추천 데이터 조회에 실패했습니다."));
        }
    }

    /**
     * 식단 추천 데이터 목록 조회
     */
    @GetMapping("/content/diet-recommendations")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getDietRecommendations(
            @PageableDefault(size = 20, sort = "recommendedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFilter) {
        log.info("🍽️ 식단 추천 데이터 목록 조회 - 검색: {}, 날짜: {}", search, dateFilter);
        
        try {
            Page<Map<String, Object>> data = adminService.getDietRecommendations(pageable, search, dateFilter);
            return ResponseEntity.ok(ApiResponseDto.success("식단 추천 데이터 조회 완료", data));
        } catch (Exception e) {
            log.error("식단 추천 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("식단 추천 데이터 조회에 실패했습니다."));
        }
    }

    /**
     * 설문 응답 데이터 목록 조회
     */
    @GetMapping("/content/surveys")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getSurveyData(
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFilter) {
        log.info("📝 설문 응답 데이터 목록 조회 - 검색: {}, 날짜: {}", search, dateFilter);
        
        try {
            Page<Map<String, Object>> data = adminService.getSurveyData(pageable, search, dateFilter);
            return ResponseEntity.ok(ApiResponseDto.success("설문 응답 데이터 조회 완료", data));
        } catch (Exception e) {
            log.error("설문 응답 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("설문 응답 데이터 조회에 실패했습니다."));
        }
    }

    /**
     * 인바디 데이터 삭제
     */
    @DeleteMapping("/content/inbody/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteInbodyData(@PathVariable Long id) {
        log.info("🗑️ 인바디 데이터 삭제 - id: {}", id);
        
        try {
            adminService.deleteInbodyData(id);
            return ResponseEntity.ok(ApiResponseDto.success("인바디 데이터가 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("인바디 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("인바디 데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 체형 분석 데이터 삭제
     */
    @DeleteMapping("/content/analysis/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteAnalysisData(@PathVariable Long id) {
        log.info("🗑️ 체형 분석 데이터 삭제 - id: {}", id);
        
        try {
            adminService.deleteAnalysisData(id);
            return ResponseEntity.ok(ApiResponseDto.success("체형 분석 데이터가 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("체형 분석 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("체형 분석 데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 운동 추천 데이터 삭제
     */
    @DeleteMapping("/content/workout-recommendations/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteWorkoutRecommendation(@PathVariable Long id) {
        log.info("🗑️ 운동 추천 데이터 삭제 - id: {}", id);
        
        try {
            adminService.deleteWorkoutRecommendation(id);
            return ResponseEntity.ok(ApiResponseDto.success("운동 추천 데이터가 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("운동 추천 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("운동 추천 데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 식단 추천 데이터 삭제
     */
    @DeleteMapping("/content/diet-recommendations/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteDietRecommendation(@PathVariable Long id) {
        log.info("🗑️ 식단 추천 데이터 삭제 - id: {}", id);
        
        try {
            adminService.deleteDietRecommendation(id);
            return ResponseEntity.ok(ApiResponseDto.success("식단 추천 데이터가 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("식단 추천 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("식단 추천 데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 설문 응답 데이터 삭제
     */
    @DeleteMapping("/content/surveys/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteSurveyData(@PathVariable Long id) {
        log.info("🗑️ 설문 응답 데이터 삭제 - id: {}", id);
        
        try {
            adminService.deleteSurveyData(id);
            return ResponseEntity.ok(ApiResponseDto.success("설문 응답 데이터가 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            log.error("설문 응답 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("설문 응답 데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    // ===== 시스템 로그 API =====

    /**
     * 활동 로그 조회
     */
    @GetMapping("/logs/activity")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getActivityLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String date) {
        log.info("📋 활동 로그 조회 - 검색: {}, 레벨: {}, 날짜: {}", search, level, date);
        
        try {
            Page<Map<String, Object>> logs = adminService.getActivityLogs(pageable, search, level, date);
            return ResponseEntity.ok(ApiResponseDto.success("활동 로그 조회 완료", logs));
        } catch (Exception e) {
            log.error("활동 로그 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("활동 로그 조회에 실패했습니다."));
        }
    }

    /**
     * 오류 로그 조회
     */
    @GetMapping("/logs/error")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getErrorLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String date) {
        log.info("❌ 오류 로그 조회 - 검색: {}, 레벨: {}, 날짜: {}", search, level, date);
        
        try {
            Page<Map<String, Object>> logs = adminService.getErrorLogs(pageable, search, level, date);
            return ResponseEntity.ok(ApiResponseDto.success("오류 로그 조회 완료", logs));
        } catch (Exception e) {
            log.error("오류 로그 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("오류 로그 조회에 실패했습니다."));
        }
    }

    /**
     * 시스템 로그 조회
     */
    @GetMapping("/logs/system")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getSystemLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String date) {
        log.info("⚙️ 시스템 로그 조회 - 검색: {}, 레벨: {}, 날짜: {}", search, level, date);
        
        try {
            Page<Map<String, Object>> logs = adminService.getSystemLogs(pageable, search, level, date);
            return ResponseEntity.ok(ApiResponseDto.success("시스템 로그 조회 완료", logs));
        } catch (Exception e) {
            log.error("시스템 로그 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("시스템 로그 조회에 실패했습니다."));
        }
    }

    /**
     * 보안 로그 조회
     */
    @GetMapping("/logs/security")
    public ResponseEntity<ApiResponseDto<Page<Map<String, Object>>>> getSecurityLogs(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String date) {
        log.info("🔒 보안 로그 조회 - 검색: {}, 레벨: {}, 날짜: {}", search, level, date);
        
        try {
            Page<Map<String, Object>> logs = adminService.getSecurityLogs(pageable, search, level, date);
            return ResponseEntity.ok(ApiResponseDto.success("보안 로그 조회 완료", logs));
        } catch (Exception e) {
            log.error("보안 로그 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDto.error("보안 로그 조회에 실패했습니다."));
        }
    }

    /**
     * 로그 내보내기
     */
    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam String type,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String date) {
        log.info("📤 로그 내보내기 - 타입: {}, 포맷: {}", type, format);
        
        try {
            byte[] csvData = adminService.exportLogs(type, format, search, level, date);
            
            String filename = String.format("%s_logs_%s.csv", type, java.time.LocalDate.now());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(csvData);
        } catch (Exception e) {
            log.error("로그 내보내기 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
