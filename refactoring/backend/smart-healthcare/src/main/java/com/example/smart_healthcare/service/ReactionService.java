package com.example.smart_healthcare.service;

import com.example.smart_healthcare.dto.request.AddReactionRequestDto;
import com.example.smart_healthcare.dto.response.ReactionCheckResponseDto;
import com.example.smart_healthcare.dto.response.ReactionResponseDto;
import com.example.smart_healthcare.entity.CommunityPost;
import com.example.smart_healthcare.entity.PostReaction;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.CommunityPostRepository;
import com.example.smart_healthcare.repository.PostReactionRepository;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {
    
    private final PostReactionRepository postReactionRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자 반응 확인 (최적화된 버전)
     */
    @Transactional(readOnly = true)
    public ReactionCheckResponseDto checkUserReaction(Long postId, Long userId) {
        log.info("사용자 반응 확인: postId={}, userId={}", postId, userId);
        
        // 직접 ID로 조회하여 성능 최적화 (논리삭제 제외)
        Optional<PostReaction> existingReaction = postReactionRepository
                .findByPostIdAndUserIdAndIsDeletedFalse(postId, userId);
        
        if (existingReaction.isPresent()) {
            PostReaction reaction = existingReaction.get();
            return new ReactionCheckResponseDto(
                    true,
                    reaction.getType(),
                    reaction.getId()
            );
        } else {
            return new ReactionCheckResponseDto(false, null, null);
        }
    }
    
    /**
     * 반응 추가/수정 (최적화된 버전)
     */
    public ReactionResponseDto addOrUpdateReaction(Long postId, AddReactionRequestDto request) {
        log.info("반응 추가/수정: postId={}, userId={}, reactionType={}", 
                postId, request.getUserId(), request.getReactionType());
        
        // 기존 반응 확인 (ID로 직접 조회, 논리삭제 제외)
        Optional<PostReaction> existingReaction = postReactionRepository
                .findByPostIdAndUserIdAndIsDeletedFalse(postId, request.getUserId());
        
        PostReaction reaction;
        if (existingReaction.isPresent()) {
            // 기존 반응 수정
            reaction = existingReaction.get();
            reaction.setType(request.getReactionType());
            log.info("기존 반응 수정: reactionId={}", reaction.getId());
        } else {
            // 새 반응 생성 (Entity 조회는 필요할 때만)
            CommunityPost post = communityPostRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));
            
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            reaction = new PostReaction();
            reaction.setPost(post);
            reaction.setUser(user);
            reaction.setType(request.getReactionType());
            log.info("새 반응 생성");
        }
        
        PostReaction savedReaction = postReactionRepository.save(reaction);
        return ReactionResponseDto.toDto(savedReaction);
    }
    
    /**
     * 반응 삭제 (최적화된 버전)
     */
    public void removeReaction(Long postId, Long userId) {
        log.info("반응 삭제: postId={}, userId={}", postId, userId);
        
        // 직접 ID로 조회하여 성능 최적화 (논리삭제 제외)
        Optional<PostReaction> existingReaction = postReactionRepository
                .findByPostIdAndUserIdAndIsDeletedFalse(postId, userId);
        
        if (existingReaction.isPresent()) {
            postReactionRepository.delete(existingReaction.get());
            log.info("반응 삭제 완료: reactionId={}", existingReaction.get().getId());
        } else {
            log.warn("삭제할 반응이 없습니다: postId={}, userId={}", postId, userId);
        }
    }
}