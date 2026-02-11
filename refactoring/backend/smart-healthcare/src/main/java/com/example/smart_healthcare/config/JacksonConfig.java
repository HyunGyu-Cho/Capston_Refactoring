package com.example.smart_healthcare.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // LocalDateTime 직렬화/역직렬화 설정
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        mapper.registerModule(javaTimeModule);

        // 숫자+단위 문자열에서 숫자만 추출해 Double로 변환하는 Deserializer 등록
        SimpleModule numModule = new SimpleModule();
        numModule.addDeserializer(Double.class, new JsonDeserializer<>() {
            private final Pattern PATTERN = Pattern.compile("[-+]?\\d*\\.?\\d+");
            @Override
            public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                Matcher m = PATTERN.matcher(text);
                if (m.find()) {
                    return Double.parseDouble(m.group());
                }
                return 0.0;
            }
        });

        mapper.registerModule(numModule);
        return mapper;
    }
} 