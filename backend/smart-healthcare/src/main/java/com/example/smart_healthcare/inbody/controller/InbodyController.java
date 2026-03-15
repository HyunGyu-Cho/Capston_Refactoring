package com.example.smart_healthcare.inbody.controller;

import com.example.smart_healthcare.auth.security.MemberUserDetails;
import com.example.smart_healthcare.common.api.ApiResponse;
import com.example.smart_healthcare.inbody.dto.request.InbodyInputRequest;
import com.example.smart_healthcare.inbody.dto.response.InbodyDetailResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodyInputResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodyListResponse;
import com.example.smart_healthcare.inbody.service.InbodyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inbody")
public class InbodyController {

    private final InbodyService inbodyService;

    @PostMapping
    public ApiResponse<InbodyInputResponse> create(
            @AuthenticationPrincipal MemberUserDetails user,
            @Valid @RequestBody InbodyInputRequest request
    ) {
        // member_id는 인증 주체에서만 가져와 저장한다.
        return ApiResponse.ok(inbodyService.create(user.getMemberId(), request));
    }

    @GetMapping("/{inbodyId}")
    public ApiResponse<InbodyDetailResponse> getDetail(
            @AuthenticationPrincipal MemberUserDetails user,
            @PathVariable Long inbodyId
    ) {
        // 본인 데이터만 조회 가능 (서비스 계층에서 member_id + inbody_id로 검증)
        return ApiResponse.ok(inbodyService.getDetail(user.getMemberId(), inbodyId));
    }

    @GetMapping("/me")
    public ApiResponse<InbodyListResponse> getMyList(
            @AuthenticationPrincipal MemberUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        // 기본은 최신 5건, 허용된 size 범위만 사용
        return ApiResponse.ok(inbodyService.getMyList(user.getMemberId(), page, size));
    }
}
