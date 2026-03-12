package com.example.smart_healthcare.auth.security;

import com.example.smart_healthcare.common.api.ApiError;
import com.example.smart_healthcare.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String code = readStringAttr(request, JwtAuthenticationFilter.AUTH_ERROR_CODE_ATTR, "AUTH-401-002");
        String message = readStringAttr(request, JwtAuthenticationFilter.AUTH_ERROR_MESSAGE_ATTR, "Access token is invalid");
        ApiError error = new ApiError(code, message, traceId(request), null);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(error)));
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute("traceId");
        return value == null ? "" : value.toString();
    }

    private String readStringAttr(HttpServletRequest request, String name, String defaultValue) {
        Object value = request.getAttribute(name);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
