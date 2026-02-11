package com.example.smart_healthcare.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads variables from a local .env file into Spring Environment as a low-priority fallback.
 *
 * This allows properties like ${OPENAI_API_KEY} to resolve from .env when OS env vars are absent.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, PriorityOrdered {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Load .env if present; ignore errors if missing/malformed
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        if (dotenv == null) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            // Only add non-empty values
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                props.put(entry.getKey(), entry.getValue());
            }
        });

        if (!props.isEmpty()) {
            // Add as lowest priority so real env/app properties can override
            MapPropertySource source = new MapPropertySource(PROPERTY_SOURCE_NAME, props);
            if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
                environment.getPropertySources().replace(PROPERTY_SOURCE_NAME, source);
            } else {
                environment.getPropertySources().addLast(source);
            }
        }
    }

    @Override
    public int getOrder() {
        // Lowest precedence to act as fallback
        return Ordered.LOWEST_PRECEDENCE;
    }
}


