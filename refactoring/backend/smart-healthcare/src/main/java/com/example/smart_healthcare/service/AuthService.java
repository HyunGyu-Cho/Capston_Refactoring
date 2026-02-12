package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import com.example.smart_healthcare.dto.response.AuthResponseDto;
import com.example.smart_healthcare.dto.response.UserResponseDto;
import com.example.smart_healthcare.entity.Member;
import com.example.smart_healthcare.repository.MemberRepository;
import com.example.smart_healthcare.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 애플리케이션 시작 후 기본 관리자 계정 생성
     */
    @PostConstruct
    @Transactional
    public void initDefaultAdmin() {
        try {
            String adminEmail = "admin@healthcare.com";
            
            // 관리자 계정이 이미 존재하는지 확인
            Optional<Member> existingAdmin = userRepository.findByEmail(adminEmail);
            
            if (existingAdmin.isEmpty()) {
                log.info("🔧 기본 관리자 계정 생성 시작: {}", adminEmail);
                
                Member admin = new Member();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123!@#"));
                admin.setRole(Member.Role.ADMIN);
                admin.setProvider(Member.AuthProvider.LOCAL);
                admin.setIsDeleted(false);
                
                Member savedAdmin = userRepository.save(admin);
                log.info("✅ 기본 관리자 계정 생성 완료: id={}, email={}", savedAdmin.getId(), savedAdmin.getEmail());
            } else {
                log.info("ℹ️ 관리자 계정이 이미 존재합니다: {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("❌ 기본 관리자 계정 생성 실패", e);
        }
    }

    /**
     * 이메일/비밀번호 회원가입
     */
    @Transactional
    public Member registerUser(String email, String password) {
        log.info("회원가입 요청: email={}", email);
        
        // 이메일 중복 체크 - findByEmail 사용으로 통일
        Optional<Member> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 팩토리 메서드를 사용하여 User 객체 생성
        Member user = Member.createLocalUser(email, passwordEncoder.encode(password));

        Member savedUser = userRepository.save(user);
        log.info("회원가입 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }

    /**
     * 이메일/비밀번호 로그인 (토큰만 반환)
     */
    public String authenticateUser(String email, String password) {
        log.info("로그인 요청: email={}", email);
        
        Member user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 비밀번호 검증 (BCrypt 우선, 평문 폴백)
        if (!isPasswordValid(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        log.info("로그인 성공: userId={}", user.getId());
        
        return token;
    }

    /**
     * 이메일/비밀번호 로그인 (토큰 + 사용자 정보 반환)
     */
    public AuthResponseDto authenticateUserWithInfo(String email, String password) {
        log.info("로그인 요청: email={}", email);
        
        Member user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 비밀번호 검증 (BCrypt 우선, 평문 폴백)
        if (!isPasswordValid(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        log.info("로그인 성공: userId={}", user.getId());
        
        return AuthResponseDto.loginSuccess(token, UserResponseDto.toDto(user));
    }

    /**
     * 관리자 로그인 (토큰 + 사용자 정보 반환)
     */
    public AuthResponseDto authenticateAdminWithInfo(String email, String password) {
        log.info("관리자 로그인 요청: email={}", email);
        
        Member user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "관리자를 찾을 수 없습니다."));

        // 관리자 권한 확인
        if (!user.getRole().equals(Member.Role.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }

        // 비밀번호 검증 (BCrypt 우선, 평문 폴백)
        if (!isPasswordValid(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        log.info("관리자 로그인 성공: userId={}", user.getId());
        
        return AuthResponseDto.loginSuccess(token, UserResponseDto.toDto(user));
    }

    // TODO: 개발 마지막 단계에서 구현 예정
    /*
    /**
     * 구글 소셜 로그인
     */
    /*
    public String authenticateGoogleUser(String accessToken) {
        log.info("구글 로그인 요청");
        
        try {
            WebClient webClient = webClientBuilder.build();
            @SuppressWarnings("unchecked")
            Map<String, Object> googleUser = webClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String email = (String) googleUser.get("email");
            String name = (String) googleUser.get("name");

            User user = findOrCreateSocialUser(User.AuthProvider.GOOGLE, email, name);
            String token = generateToken(user);
            
            log.info("구글 로그인 성공: userId={}", user.getId());
            return token;
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "구글 로그인 처리 중 오류: " + e.getMessage());
        }
    }
    */

    /*
    /**
     * 카카오 소셜 로그인
     */
    /*
    public String authenticateKakaoUser(String accessToken) {
        log.info("카카오 로그인 요청");
        
        try {
            WebClient webClient = webClientBuilder.build();
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoResponse = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoResponse.get("kakao_account");
            String email = (String) kakaoAccount.get("email");
            @SuppressWarnings("unchecked")
            String name = (String) ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname");

            User user = findOrCreateSocialUser(User.AuthProvider.KAKAO, email, name);
            String token = generateToken(user);
            
            log.info("카카오 로그인 성공: userId={}", user.getId());
            return token;
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "카카오 로그인 처리 중 오류: " + e.getMessage());
        }
    }
    */

    /*
    /**
     * 네이버 소셜 로그인
     */
    /*
    public String authenticateNaverUser(String accessToken) {
        log.info("네이버 로그인 요청");
        
        try {
            WebClient webClient = webClientBuilder.build();
            @SuppressWarnings("unchecked")
            Map<String, Object> naverResponse = webClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) naverResponse.get("response");
            String email = (String) response.get("email");
            String name = (String) response.get("name");

            User user = findOrCreateSocialUser(User.AuthProvider.NAVER, email, name);
            String token = generateToken(user);
            
            log.info("네이버 로그인 성공: userId={}", user.getId());
            return token;
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "네이버 로그인 처리 중 오류: " + e.getMessage());
        }
    }
    */

    /*
    /**
     * 소셜 로그인 사용자 조회 또는 생성
     */
    /*
    @Transactional
    private User findOrCreateSocialUser(User.AuthProvider provider, String email, String name) {
        Optional<User> existingUser = userRepository.findByEmailAndProvider(email, provider);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setProvider(provider);
        newUser.setRole(User.Role.USER);
        // createdAt은 BaseEntity에서 자동 설정됨

        User savedUser = userRepository.save(newUser);
        log.info("소셜 사용자 생성: id={}, email={}, provider={}", savedUser.getId(), savedUser.getEmail(), provider);
        
        return savedUser;
    }
    */

    /**
     * JWT 토큰 생성
     */
    private String generateToken(Member user) {
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
    
    /**
     * 이메일로 사용자 찾기 (관리자 기능용)
     */
    public Member findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 이메일로 사용자 찾기 (Optional 반환)
     */
    public Optional<Member> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 사용자 저장
     */
    @Transactional
    public Member saveUser(Member user) {
        return userRepository.save(user);
    }

    /**
     * 비밀번호 검증 (BCrypt 우선, 평문 폴백)
     * - BCrypt로 암호화된 경우: passwordEncoder.matches() 사용
     * - 평문인 경우: 직접 비교 후 자동 마이그레이션
     */
    private boolean isPasswordValid(String rawPassword, String storedPassword) {
        // BCrypt로 암호화된 경우 (일반적인 경우)
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }
        
        // 평문인 경우 (기존 사용자 마이그레이션)
        if (rawPassword.equals(storedPassword)) {
            log.warn("평문 비밀번호 감지 - 자동 마이그레이션 필요: storedPassword={}", storedPassword);
            // 여기서는 검증만 하고, 마이그레이션은 별도로 처리
            return true;
        }
        
        return false;
    }

    /*
    /**
     * 소셜 로그인 사용자 조회
     */
    /*
    public User findByEmailAndProvider(String email, User.AuthProvider provider) {
        return userRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
    */
}
