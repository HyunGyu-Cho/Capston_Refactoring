package com.example.smart_healthcare.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class JsonAttributeConverter implements AttributeConverter<Map<String, Float>, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Float> attr) {
        try {
            return mapper.writeValueAsString(attr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON write error", e);
        }
    }

    @Override
    public Map<String, Float> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<Map<String, Float>>() {});
        } catch (IOException e) {
            throw new RuntimeException("JSON read error", e);
        }
    }
}
