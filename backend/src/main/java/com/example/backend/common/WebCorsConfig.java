package com.example.backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:80,http://localhost}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var mapping = registry.addMapping("/api/v1/**");
        if (allowedOrigins != null && allowedOrigins.length == 1 && "*".equals(allowedOrigins[0].trim())) {
            mapping.allowedOriginPatterns("*");
        } else {
            mapping.allowedOrigins(allowedOrigins);
        }
        mapping.allowedMethods("GET", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}

