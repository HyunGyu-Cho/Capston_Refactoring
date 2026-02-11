package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.entity.*;
import com.example.smart_healthcare.service.CommunityService;
import com.example.smart_healthcare.service.CommentService;
import com.example.smart_healthcare.service.ReactionService;
import com.example.smart_healthcare.entity.CommunityPost.PostCategory;
import com.example.smart_healthcare.dto.request.PostRequestDto;
import com.example.smart_healthcare.dto.response.PostResponseDto;
import com.example.smart_healthcare.dto.response.CommentResponseDto;
import com.example.smart_healthcare.dto.request.AddReactionRequestDto;
import com.example.smart_healthcare.dto.response.ReactionCheckResponseDto;
import com.example.smart_healthcare.dto.response.ReactionResponseDto;
import com.example.smart_healthcare.common.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommunityController.class);
    private final CommunityService communityService;
    private final CommentService commentService;
    private final ReactionService reactionService;

    // ===== 게시글 관리 =====
    
    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponseDto<PostResponseDto>> createPost(@Valid @RequestBody PostRequestDto request) {
        try {
            logger.info("게시글 작성 요청: title={}, category={}", request.getTitle(), request.getCategory());
            
            //CommunityPost post = communityService.createPost(request);
            //PostResponseDto response = PostResponseDto.toDto(post);

            PostResponseDto response = communityService.createPost(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("게시글이 성공적으로 생성되었습니다.", response));
        } catch (Exception e) {
            logger.error("게시글 작성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("게시글 작성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 통합 게시글 목록 조회 (페이지네이션, 정렬, 검색, 필터)
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sortType 정렬 타입 (recent, popular, comments, views)
     * @param search 통합 검색어 (제목 OR 내용)
     * @param title 제목 검색어
     * @param content 내용 검색어
     * @param category 카테고리 필터
     * @param authorId 작성자 ID 필터
     * @param startDate 시작일
     * @param endDate 종료일
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<PostResponseDto>>> list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortType", defaultValue = "recent") String sortType,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "category", required = false) PostCategory category,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        try {
            logger.info("게시글 목록 조회 요청: page={}, size={}, sortType={}, search={}, title={}, content={}, category={}, authorId={}, startDate={}, endDate={}", 
                    page, size, sortType, search, title, content, category, authorId, startDate, endDate);
            
            // Date를 LocalDateTime으로 변환
            LocalDateTime startDateTime = startDate != null ? 
                startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            LocalDateTime endDateTime = endDate != null ? 
                endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            
            Page<PostResponseDto> posts = communityService.getPosts(
                page, size, sortType, search, title, content, 
                category, authorId, startDateTime, endDateTime
            );
            
            logger.info("게시글 목록 조회 완료: {} 건", posts.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("게시글 목록 조회가 완료되었습니다.", posts));
        } catch (Exception e) {
            logger.error("게시글 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("게시글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // 게시글 상세 조회 (조회수 증가)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PostResponseDto>> getPost(@PathVariable Long id) {
        try {
            logger.info("게시글 상세 조회 요청: id={}", id);
            CommunityPost post = communityService.getPostById(id);
            PostResponseDto response = PostResponseDto.toDto(post);
            return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success("게시글 조회가 완료되었습니다.", response));
        } catch (Exception e) {
            logger.error("게시글 상세 조회 실패: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("게시글 조회에 실패했습니다: " + e.getMessage()));
        }
    }


    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommunityPost> updatePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        try {
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            PostCategory category = PostCategory.valueOf((String) request.get("category"));
            @SuppressWarnings("unchecked")
            Set<String> tags = new HashSet<>((List<String>) request.get("tags"));
            Long authorId = Long.valueOf(request.get("authorId").toString());

            CommunityPost post = communityService.updatePost(id, title, content, category, tags, authorId);
            return ResponseEntity.status(HttpStatus.OK).body(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deletePost(@PathVariable Long id, @RequestParam(required = false) Long authorId) {
        try {
            logger.info("게시글 삭제 요청: id={}, authorId={}", id, authorId);
            communityService.deletePost(id, authorId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("게시글이 성공적으로 삭제되었습니다.", "SUCCESS"));
        } catch (Exception e) {
            logger.error("게시글 삭제 실패: id={}, authorId={}", id, authorId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("게시글 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    // ===== 추천/비추천 시스템 =====
    // 반응 관련 기능이 CommunityController에 통합되었습니다.
    // 관련 API: /api/reactions/posts/{postId}

    // ===== 댓글 시스템 =====
    
    /**
     * 게시글별 댓글 목록 조회 (페이징)
     * 
     * 모든 댓글(최상위 댓글 + 대댓글)을 생성일순으로 조회합니다.
     * 프론트엔드에서 parentId를 확인하여 댓글 계층 구조를 표시할 수 있습니다.
     * - parentId가 null: 최상위 댓글
     * - parentId가 있음: 대댓글
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponseDto<Page<CommentResponseDto>>> getComments(
            @PathVariable Long postId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            logger.info("댓글 목록 조회 요청: postId={}, page={}, size={}", postId, page, size);
            
            Page<CommentResponseDto> comments = commentService.getCommentsByPostId(postId, page, size);
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("댓글 목록 조회가 완료되었습니다.", comments));
        } catch (Exception e) {
            logger.error("댓글 목록 조회 실패: postId={}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("댓글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            logger.info("댓글 작성 요청: postId={}, request={}", postId, request);
            
            // 요청 데이터 검증
            if (request.get("content") == null) {
                logger.error("댓글 내용이 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("댓글 내용이 필요합니다."));
            }
            
            if (request.get("authorId") == null) {
                logger.error("작성자 ID가 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("작성자 ID가 필요합니다."));
            }
            
            String content = (String) request.get("content");
            Long authorId;
            try {
                authorId = Long.valueOf(request.get("authorId").toString());
            } catch (NumberFormatException e) {
                logger.error("authorId 형식이 잘못되었습니다: {}", request.get("authorId"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("작성자 ID 형식이 올바르지 않습니다."));
            }
            
            Long parentId = request.get("parentId") != null ? 
                Long.valueOf(request.get("parentId").toString()) : null;

            logger.info("파싱된 댓글 데이터: content={}, authorId={}, parentId={}", content, authorId, parentId);

            CommentResponseDto comment = commentService.createComment(postId, content, authorId, parentId);
            logger.info("댓글 작성 성공: commentId={}", comment.id());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("댓글이 성공적으로 작성되었습니다.", comment));
        } catch (Exception e) {
            logger.error("댓글 작성 실패: postId={}, request={}", postId, request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("댓글 작성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            logger.info("댓글 수정 요청: commentId={}, request={}", commentId, request);
            
            // 요청 데이터 검증
            if (request.get("content") == null) {
                logger.error("댓글 내용이 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("댓글 내용이 필요합니다."));
            }
            
            if (request.get("authorId") == null) {
                logger.error("작성자 ID가 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseDto.error("작성자 ID가 필요합니다."));
            }
            
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            CommentResponseDto updatedComment = commentService.updateComment(commentId, content, authorId);
            logger.info("댓글 수정 성공: commentId={}", commentId);
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("댓글이 성공적으로 수정되었습니다.", updatedComment));
        } catch (Exception e) {
            logger.error("댓글 수정 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("댓글 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 댓글 삭제 (논리삭제)
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long authorId
    ) {
        try {
            logger.info("댓글 삭제 요청: commentId={}, authorId={}", commentId, authorId);
            
            commentService.deleteComment(commentId, authorId);
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.", null));
        } catch (Exception e) {
            logger.error("댓글 삭제 실패: commentId={}, authorId={}", commentId, authorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("댓글 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 관리자 댓글 삭제 (논리삭제)
     */
    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCommentByAdmin(
            @PathVariable Long commentId
    ) {
        try {
            logger.info("관리자 댓글 삭제 요청: commentId={}", commentId);
            
            commentService.deleteCommentByAdmin(commentId);
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.", null));
        } catch (Exception e) {
            logger.error("관리자 댓글 삭제 실패: commentId={}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("댓글 삭제에 실패했습니다: " + e.getMessage()));
        }
    }


    // ===== 반응 관리 =====
    
    /**
     * 사용자 반응 확인
     */
    @GetMapping("/{postId}/reaction/check")
    public ResponseEntity<ApiResponseDto<ReactionCheckResponseDto>> checkUserReaction(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        logger.info("사용자 반응 확인 API 호출: postId={}, userId={}", postId, userId);
        
        try {
            ReactionCheckResponseDto response = reactionService.checkUserReaction(postId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("반응 확인이 완료되었습니다.", response));
        } catch (Exception e) {
            logger.error("반응 확인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("반응 확인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 반응 추가/수정
     */
    @PostMapping("/{postId}/reaction")
    public ResponseEntity<ApiResponseDto<ReactionResponseDto>> addReaction(
            @PathVariable Long postId,
            @Valid @RequestBody AddReactionRequestDto request) {
        logger.info("반응 추가/수정 API 호출: postId={}, userId={}, reactionType={}", 
                postId, request.getUserId(), request.getReactionType());
        
        try {
            ReactionResponseDto response = reactionService.addOrUpdateReaction(postId, request);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("반응이 처리되었습니다.", response));
        } catch (Exception e) {
            logger.error("반응 추가/수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("반응 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 반응 삭제
     */
    @DeleteMapping("/{postId}/reaction")
    public ResponseEntity<ApiResponseDto<Void>> removeReaction(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        logger.info("반응 삭제 API 호출: postId={}, userId={}", postId, userId);
        
        try {
            reactionService.removeReaction(postId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("반응이 삭제되었습니다.", null));
        } catch (Exception e) {
            logger.error("반응 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("반응 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
