package com.example.smart_healthcare.auth.api;

import com.example.smart_healthcare.auth.application.AuthService;
import com.example.smart_healthcare.auth.dto.request.LoginRequest;
import com.example.smart_healthcare.auth.dto.request.SignupRequest;
import com.example.smart_healthcare.auth.dto.response.LoginResponse;
import com.example.smart_healthcare.auth.dto.response.MeResponse;
import com.example.smart_healthcare.auth.dto.response.SignupResponse;
import com.example.smart_healthcare.auth.security.MemberUserDetails;
import com.example.smart_healthcare.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;

    @Value("${app.auth.cookie.secure:true}")
    private boolean secureCookie;

    @Value("${app.auth.jwt.refresh-expire-sec:604800}")
    private long refreshExpireSec;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), false).toString())
                .body(ApiResponse.ok(result.response()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        AuthService.AuthResult result = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), false).toString())
                .body(ApiResponse.ok(result.response()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", true).toString())
                .body(ApiResponse.ok("success"));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal MemberUserDetails user) {
        return ApiResponse.ok(authService.me(user));
    }

    private ResponseCookie refreshCookie(String token, boolean expireNow) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/v1/auth")
                .sameSite("Strict");
        if (expireNow) {
            builder.maxAge(0);
        } else {
            builder.maxAge(refreshExpireSec);
        }
        return builder.build();
    }
}
