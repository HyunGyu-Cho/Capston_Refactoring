package com.example.smart_healthcare.controller;

import com.example.smart_healthcare.common.dto.ApiResponseDto;
import com.example.smart_healthcare.dto.request.LoginRequestDto;
import com.example.smart_healthcare.dto.request.SignupRequestDto;
import com.example.smart_healthcare.dto.response.AuthResponseDto;
import com.example.smart_healthcare.dto.response.UserResponseDto;
// import com.example.smart_healthcare.dto.request.SocialLoginRequestDto; // 주석처리됨
import com.example.smart_healthcare.entity.User;
import com.example.smart_healthcare.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관리 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일/비밀번호 회원가입
     */
    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        log.info("회원가입 API 호출: email={}", request.getEmail());
        
        try {
            User user = authService.registerUser(request.getEmail(), request.getPassword());
            String token = authService.authenticateUser(request.getEmail(), request.getPassword());
            
            AuthResponseDto authResponse = AuthResponseDto.signupSuccess(token, UserResponseDto.toDto(user));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("회원가입이 완료되었습니다.", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 이메일/비밀번호 로그인
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("로그인 API 호출: email={}", request.getEmail());
        
        try {
            // 로그인과 사용자 정보를 한 번에 처리 (DB 쿼리 1회로 최적화)
            AuthResponseDto authResponse = authService.authenticateUserWithInfo(request.getEmail(), request.getPassword());
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("로그인 성공", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    /**
     * 관리자 로그인
     */
    @Operation(summary = "관리자 로그인", description = "관리자 이메일과 비밀번호로 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/admin-login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> adminLogin(@Valid @RequestBody LoginRequestDto request) {
        log.info("관리자 로그인 API 호출: email={}", request.getEmail());
        
        try {
            // 관리자 로그인과 사용자 정보를 한 번에 처리
            AuthResponseDto authResponse = authService.authenticateAdminWithInfo(request.getEmail(), request.getPassword());
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.success("관리자 로그인 성공", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    // TODO: 개발 마지막 단계에서 구현 예정
    /*
    /**
     * 구글 소셜 로그인
     */
    /*
    @Operation(summary = "구글 로그인", description = "구글 소셜 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody SocialLoginRequestDto request) {
        log.info("구글 로그인 API 호출: email={}", request.getEmail());
        
        try {
            String token = authService.authenticateGoogleUser(request.getAccessToken());
            User user = authService.findByEmailAndProvider(request.getEmail(), User.AuthProvider.GOOGLE);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                        "success", true,
                        "token", token,
                        "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "provider", user.getProvider()
                        )
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }
    */

    /*
    /**
     * 카카오 소셜 로그인
     */
    /*
    @Operation(summary = "카카오 로그인", description = "카카오 소셜 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@Valid @RequestBody SocialLoginRequestDto request) {
        log.info("카카오 로그인 API 호출: email={}", request.getEmail());
        
        try {
            String token = authService.authenticateKakaoUser(request.getAccessToken());
            User user = authService.findByEmailAndProvider(request.getEmail(), User.AuthProvider.KAKAO);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                        "success", true,
                        "token", token,
                        "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "provider", user.getProvider()
                        )
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }
    */

    /*
    /**
     * 네이버 소셜 로그인
     */
    /*
    @Operation(summary = "네이버 로그인", description = "네이버 소셜 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@Valid @RequestBody SocialLoginRequestDto request) {
        log.info("네이버 로그인 API 호출: email={}", request.getEmail());
        
        try {
            String token = authService.authenticateNaverUser(request.getAccessToken());
            User user = authService.findByEmailAndProvider(request.getEmail(), User.AuthProvider.NAVER);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                        "success", true,
                        "token", token,
                        "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "provider", user.getProvider()
                        )
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }
    */


}
