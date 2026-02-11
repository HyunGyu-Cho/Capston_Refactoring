package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.dto.response.CommentResponseDto;
import com.example.smart_healthcare.entity.Comment;
import com.example.smart_healthcare.entity.CommunityPost;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.CommentRepository;
import com.example.smart_healthcare.repository.CommunityPostRepository;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    
    private final CommentRepository commentRepo;
    private final CommunityPostRepository communityPostRepo;
    private final UserRepository userRepo;
    
    // ===== 댓글 조회 =====
    
    /**
     * 게시글별 댓글 목록 조회 (페이징)
     * 
     * 모든 댓글(최상위 댓글 + 대댓글)을 생성일순으로 조회합니다.
     * 프론트엔드에서 parentId를 기준으로 계층 구조를 구성할 수 있습니다.
     */
    public Page<CommentResponseDto> getCommentsByPostId(Long postId, int page, int size) {
        log.info("댓글 목록 조회: postId={}, page={}, size={}", postId, page, size);
        
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<Comment> comments = commentRepo.findByPostIdAndNotDeleted(postId, pageable);
        
        return comments.map(CommentResponseDto::toDto);
    }
    
    // ===== 댓글 생성 =====
    
    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponseDto createComment(Long postId, String content, Long authorId, Long parentId) {
        log.info("댓글 생성 시작: postId={}, authorId={}, parentId={}", postId, authorId, parentId);
        
        // 게시글 존재 확인
        CommunityPost post = communityPostRepo.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글을 찾을 수 없습니다: postId={}", postId);
                    return new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
                });
        
        // 사용자 존재 확인
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: authorId={}", authorId);
                    return new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        Comment comment;
        
        // 대댓글인 경우
        if (parentId != null) {
            // 부모 댓글 존재 확인
            Comment parentComment = commentRepo.findById(parentId)
                    .orElseThrow(() -> {
                        log.error("부모 댓글을 찾을 수 없습니다: parentId={}", parentId);
                        return new BusinessException(ErrorCode.NOT_FOUND, "부모 댓글을 찾을 수 없습니다.");
                    });
            
            // 부모 댓글이 삭제되었는지 확인
            if (parentComment.getIsDeleted()) {
                log.error("삭제된 댓글에 답글을 작성할 수 없습니다: parentId={}", parentId);
                throw new BusinessException(ErrorCode.BAD_REQUEST, "삭제된 댓글에는 답글을 작성할 수 없습니다.");
            }
            
            // 부모 댓글이 같은 게시글에 속하는지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                log.error("부모 댓글과 게시글이 일치하지 않습니다: parentId={}, postId={}", parentId, postId);
                throw new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 댓글 참조입니다.");
            }
            
            // 대댓글 깊이 제한 (최대 2단계)
            if (parentComment.getParentId() != null) {
                log.error("대댓글에는 답글을 작성할 수 없습니다: parentId={}", parentId);
                throw new BusinessException(ErrorCode.BAD_REQUEST, "대댓글에는 답글을 작성할 수 없습니다.");
            }
            
            // Entity의 팩토리 메서드 사용 (내부에서 검증 수행)
            comment = Comment.createReply(content, author, post, parentId);
            log.info("대댓글 생성 완료: parentId={}", parentId);
        } else {
            // Entity의 팩토리 메서드 사용 (내부에서 검증 수행)
            comment = Comment.createComment(content, author, post);
            log.info("일반 댓글 생성 완료");
        }
        
        Comment savedComment = commentRepo.save(comment);
        log.info("댓글 저장 완료: commentId={}", savedComment.getId());
        
        // TODO: 댓글 알림 기능은 향후 구현 예정
        // createCommentNotification(post, author, savedComment);
        
        return CommentResponseDto.toDto(savedComment);
    }
    
    // ===== 댓글 수정 =====
    
    /**
     * 댓글 수정 (PATCH 방식)
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String content, Long authorId) {
        log.info("댓글 수정 시작: commentId={}, authorId={}", commentId, authorId);
        
        // 댓글 존재 확인
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        
        // Entity의 patch 메서드 사용 (권한 확인 및 검증 포함)
        comment.patch(content, authorId);
        
        log.info("댓글 수정 완료: commentId={}, authorId={}", commentId, authorId);
        return CommentResponseDto.toDto(commentRepo.save(comment));
    }
    
    // ===== 댓글 삭제 =====
    
    /**
     * 댓글 삭제 (논리삭제)
     */
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        log.info("댓글 삭제 시작: commentId={}, authorId={}", commentId, authorId);
        
        // 댓글 존재 확인
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "댓글을 삭제할 권한이 없습니다.");
        }
        
        // 삭제 상태 확인
        if (comment.getIsDeleted()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 삭제된 댓글입니다.");
        }
        
        // 논리삭제 (BaseEntity의 delete() 메서드 사용)
        comment.delete();
        commentRepo.save(comment);
        
        log.info("댓글 삭제 완료: commentId={}, authorId={}", commentId, authorId);
    }
    
    /**
     * 관리자 댓글 삭제 (논리삭제)
     */
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        log.info("관리자 댓글 삭제 시작: commentId={}", commentId);
        
        // 댓글 존재 확인
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        
        // 삭제 상태 확인
        if (comment.getIsDeleted()) {
            log.error("이미 삭제된 댓글입니다: commentId={}", commentId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 삭제된 댓글입니다.");
        }
        
        // 논리삭제 (BaseEntity의 delete() 메서드 사용)
        comment.delete();
        commentRepo.save(comment);
        
        log.info("관리자 댓글 삭제 완료: commentId={}", commentId);
    }
    
    // ===== 알림 관리 =====
    // TODO: 알림 기능은 향후 구현 예정
    /*
    private void createCommentNotification(CommunityPost post, User commenter, Comment comment) {
        // 댓글 알림 로직 구현
    }
    */
}
