package com.example.smart_healthcare.common.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP"));
    }

    @GetMapping("/api/v1/ready")
    public ApiResponse<Map<String, String>> ready() {
        return ApiResponse.ok(Map.of("status", "READY"));
    }
}
