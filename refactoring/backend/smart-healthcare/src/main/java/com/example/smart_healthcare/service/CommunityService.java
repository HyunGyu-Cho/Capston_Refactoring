package com.example.smart_healthcare.service;

import com.example.smart_healthcare.entity.*;
import com.example.smart_healthcare.repository.*;
import com.example.smart_healthcare.entity.CommunityPost.PostCategory;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import com.example.smart_healthcare.dto.request.PostRequestDto;
import com.example.smart_healthcare.dto.response.PostResponseDto;
import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommunityService {
    
    private final CommunityPostRepository communityPostRepo;
    private final UserRepository userRepo;
    private final PostReactionRepository postReactionRepo;

    // ===== 게시글 관리 =====
    
    /**
     * 게시글 작성
     */
    @Transactional
    public PostResponseDto createPost(PostRequestDto request) {
        // 사용자 조회
        User author = userRepo.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getAuthorId()));
        
        // Builder 패턴으로 엔티티 생성
        CommunityPost post = CommunityPost.createPost(
                request.getTitle(),
                request.getContent(),
                author,
                request.getCategory(),
                request.getTags()
        );
        
        // 저장 및 반환
        CommunityPost savedPost = communityPostRepo.save(post);
        return PostResponseDto.toDto(savedPost);
    }
    
    /**
     * 통합 게시글 목록 조회 (페이지네이션, 정렬, 검색, 필터)
     */
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(
            int page, int size, String sortType,
            String search, String title, String content,
            PostCategory category, Long authorId,
            LocalDateTime startDate, LocalDateTime endDate
    ) {
        // 정렬 키 결정
        String sortProperty = switch (sortType != null ? sortType : "recent") {
            case "popular"  -> "likeCount";      // 좋아요 수
            case "comments" -> "commentCount";   // 댓글 수
            case "views"    -> "viewCount";      // 조회수
            case "recent"   -> "createdAt";      // 최신순
            default         -> "createdAt";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortProperty));
        
        // 검색 조건이 있으면 고급 검색, 없으면 기본 조회
        Page<CommunityPost> posts;
        if (hasSearchConditions(search, title, content, category, authorId, startDate, endDate)) {
            // 검색어에 % 추가 (LIKE 쿼리용)
            String searchKeyword = search != null && !search.isBlank() ? "%" + search + "%" : null;
            String searchTitle = title != null && !title.isBlank() ? "%" + title + "%" : null;
            String searchContent = content != null && !content.isBlank() ? "%" + content + "%" : null;
            
            posts = communityPostRepo.findByAdvancedSearch(
                searchKeyword, searchTitle, searchContent, category, authorId, startDate, endDate, pageable
            );
        } else {
            posts = communityPostRepo.findByIsDeletedFalse(pageable);
        }
        
        // CommunityPost 엔티티를 PostResponseDto로 변환
        return posts.map(PostResponseDto::toDto);
    }

    /**
     * 검색 조건 존재 여부 확인
     */
    private boolean hasSearchConditions(String search, String title, String content,
                                      PostCategory category, Long authorId,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        return (search != null && !search.isBlank()) ||
               (title != null && !title.isBlank()) ||
               (content != null && !content.isBlank()) ||
               (category != null) ||
               (authorId != null) ||
               (startDate != null) ||
               (endDate != null);
    }

    /**
     * 게시글 상세 조회 (조회수 증가, 반응 수 실시간 계산)
     */
    @Transactional
    public CommunityPost getPostById(Long id) {
        CommunityPost post = communityPostRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 조회수 증가
        post.incrementViewCount();
        
        // 반응 수 실시간 계산
        long likeCount = postReactionRepo.countByPostIdAndTypeAndIsDeletedFalse(id, PostReaction.ReactionType.LIKE);
        long dislikeCount = postReactionRepo.countByPostIdAndTypeAndIsDeletedFalse(id, PostReaction.ReactionType.DISLIKE);
        
        post.updateReactionCounts((int) likeCount, (int) dislikeCount);
        
        return communityPostRepo.save(post);
    }


    /**
     * 게시글 수정 (PATCH 방식)
     */
    @Transactional
    public CommunityPost updatePost(Long id, String title, String content, PostCategory category,
                                 Set<String> tags, Long authorId) {
        log.info("게시글 수정 시작: id={}, authorId={}", id, authorId);
        
        CommunityPost post = communityPostRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        
        // Entity의 patch 메서드 사용 (권한 확인 및 검증 포함)
        post.patch(title, content, category, tags, authorId);
        
        log.info("게시글 수정 완료: id={}, authorId={}", id, authorId);
        return communityPostRepo.save(post);
    }

    /**
     * 게시글 삭제 (논리삭제)
     */
    @Transactional
    public void deletePost(Long id, Long authorId) {
        CommunityPost post = communityPostRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("게시글을 삭제할 권한이 없습니다.");
        }
        
        // 논리삭제 (BaseEntity의 delete() 메서드 사용)
        post.delete();
        communityPostRepo.save(post);
    }
    
    /**
     * 관리자 게시글 삭제 (논리삭제)
     */
    @Transactional
    public void deletePostByAdmin(Long id) {
        log.info("관리자 게시글 삭제 시작: id={}", id);
        
        CommunityPost post = communityPostRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        
        // 삭제 상태 확인
        if (post.getIsDeleted()) {
            log.error("이미 삭제된 게시글입니다: id={}", id);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 삭제된 게시글입니다.");
        }
        
        // 논리삭제 (BaseEntity의 delete() 메서드 사용)
        post.delete();
        communityPostRepo.save(post);
        
        log.info("관리자 게시글 삭제 완료: id={}", id);
    }   


    
}