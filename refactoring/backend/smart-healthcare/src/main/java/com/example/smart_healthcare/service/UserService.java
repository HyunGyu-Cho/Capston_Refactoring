package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.dto.request.UpdateUserRequestDto;
import com.example.smart_healthcare.dto.response.UserResponseDto;
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;
    
    
    /**
     * 사용자 조회 (ID)
     */
    public UserResponseDto getUserById(Long id) {
        log.info("사용자 조회 요청: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        return UserResponseDto.toDto(user);
    }
    
    /**
     * 사용자 조회 (이메일)
     */
    public UserResponseDto getUserByEmail(String email) {
        log.info("사용자 조회 요청: email={}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        return UserResponseDto.toDto(user);
    }
    
    /*
    /**
     * 사용자 조회 (이메일 + 제공자)
     */
    /*
    public UserResponseDto getUserByEmailAndProvider(String email, User.AuthProvider provider) {
        log.info("사용자 조회 요청: email={}, provider={}", email, provider);
        
        User user = userRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        return UserResponseDto.fromEntity(user);
    }
    */
    
    /**
     * 사용자 목록 조회
     */
    public List<UserResponseDto> getAllUsers() {
        log.info("전체 사용자 목록 조회 요청");
        
        return userRepository.findAll().stream()
                .map(UserResponseDto::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 역할별 사용자 목록 조회
     */
    public List<UserResponseDto> getUsersByRole(User.Role role) {
        log.info("역할별 사용자 목록 조회 요청: role={}", role);
        
        return userRepository.findByRole(role).stream()
                .map(UserResponseDto::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        log.info("사용자 정보 수정 요청: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        // 이메일 변경 시 중복 확인
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }
        
        // DTO -> Entity 업데이트
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        // 비밀번호 암호화
        if (request.getPassword() != null) {
            // user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPassword(request.getPassword()); // 임시로 평문 저장
        }
        
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: id={}", updatedUser.getId());
        
        return UserResponseDto.toDto(updatedUser);
    }
    
    /**
     * 사용자 삭제 (논리 삭제)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("사용자 삭제 요청: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        
        user.delete(); // 논리 삭제
        userRepository.save(user);
        
        log.info("사용자 삭제 완료: id={}", id);
    }
    
    /**
     * 사용자 통계 조회
     */
    public UserStatisticsDto getUserStatistics() {
        log.info("사용자 통계 조회 요청");
        
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRole(User.Role.ADMIN);
        // TODO: 소셜 로그인 구현 시 활성화
        /*
        long localUsers = userRepository.countByProvider(User.AuthProvider.LOCAL);
        long googleUsers = userRepository.countByProvider(User.AuthProvider.GOOGLE);
        long kakaoUsers = userRepository.countByProvider(User.AuthProvider.KAKAO);
        long naverUsers = userRepository.countByProvider(User.AuthProvider.NAVER);
        */
        long localUsers = 0; // 임시값
        long googleUsers = 0; // 임시값
        long kakaoUsers = 0; // 임시값
        long naverUsers = 0; // 임시값
        
        return UserStatisticsDto.builder()
                .totalUsers(totalUsers)
                .adminUsers(adminUsers)
                .localUsers(localUsers)
                .googleUsers(googleUsers)
                .kakaoUsers(kakaoUsers)
                .naverUsers(naverUsers)
                .build();
    }
    
    /**
     * 사용자 통계 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class UserStatisticsDto {
        private final long totalUsers;
        private final long adminUsers;
        private final long localUsers;
        private final long googleUsers;
        private final long kakaoUsers;
        private final long naverUsers;
    }
}
