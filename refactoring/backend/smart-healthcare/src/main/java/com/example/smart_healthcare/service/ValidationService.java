package com.example.smart_healthcare.service;

import com.example.smart_healthcare.common.error.BusinessException;
import com.example.smart_healthcare.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ValidationService {
    
    /**
     * 필수 필드 검증
     */
    public static Map<String, Object> validate(Map<String, Object> body, List<String> features) {
        log.info("필드 검증 요청: features={}", features);
        
        Map<String, Object> validatedData = new HashMap<>();
        
        for (String feature : features) {
            Object value = body.get(feature);
            if (value == null) {
                log.error("필수 필드 누락: {}", feature);
                throw new BusinessException(ErrorCode.BAD_REQUEST, feature + " 필드가 없습니다.");
            }
            validatedData.put(feature, value);
        }
        
        log.info("필드 검증 완료: validatedFields={}", validatedData.keySet());
        return validatedData;
    }
    
    /**
     * 이메일 형식 검증
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * 비밀번호 강도 검증
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // 최소 8자 이상, 영문자, 숫자, 특수문자 포함
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordRegex);
    }
    
    /**
     * 숫자 범위 검증
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        
        double doubleValue = value.doubleValue();
        double doubleMin = min.doubleValue();
        double doubleMax = max.doubleValue();
        
        return doubleValue >= doubleMin && doubleValue <= doubleMax;
    }
    
    /**
     * 문자열 길이 검증
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
}