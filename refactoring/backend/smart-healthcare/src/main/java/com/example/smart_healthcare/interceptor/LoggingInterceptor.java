package com.example.smart_healthcare.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * HTTP 요청/응답을 자동으로 로깅하는 인터셉터
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // 요청 시작 시간과 ID를 request 속성에 저장
        request.setAttribute(START_TIME, startTime);
        request.setAttribute(REQUEST_ID, requestId);
        
        // MDC에 요청 정보 설정
        MDC.put("requestId", requestId);
        MDC.put("ipAddress", getClientIpAddress(request));
        MDC.put("endpoint", request.getRequestURI());
        MDC.put("method", request.getMethod());
        
        // 요청 시작 로깅
        logger.info("🚀 요청 시작 | {} {} | ID: {} | IP: {} | User-Agent: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            requestId,
            getClientIpAddress(request),
            request.getHeader("User-Agent")
        );
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // postHandle에서는 특별한 로깅 없음
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute(START_TIME);
        String requestId = (String) request.getAttribute(REQUEST_ID);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // 응답 정보 설정
        MDC.put("statusCode", String.valueOf(response.getStatus()));
        MDC.put("responseTime", String.valueOf(executionTime));
        
        // 요청 완료 로깅
        if (ex != null) {
            // 예외 발생 시
            logger.error("❌ 요청 실패 | {} {} | ID: {} | Status: {} | Time: {}ms | Error: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                requestId,
                response.getStatus(),
                executionTime,
                ex.getMessage()
            );
        } else {
            // 정상 완료 시
            if (response.getStatus() >= 400) {
                logger.warn("⚠️ 요청 경고 | {} {} | ID: {} | Status: {} | Time: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    requestId,
                    response.getStatus(),
                    executionTime
                );
            } else {
                logger.info("✅ 요청 완료 | {} {} | ID: {} | Status: {} | Time: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    requestId,
                    response.getStatus(),
                    executionTime
                );
            }
        }
        
        // 성능 로깅
        if (executionTime > 1000) {
            logger.warn("🐌 성능 경고 - {} | 실행시간: {}ms", request.getRequestURI(), executionTime);
        } else if (executionTime > 500) {
            logger.info("⏱️ 성능 정보 - {} | 실행시간: {}ms", request.getRequestURI(), executionTime);
        }
        
        // MDC 정리
        MDC.clear();
    }
    
    /**
     * 클라이언트 IP 주소 추출 (LoggingUtils와 동일한 로직)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
