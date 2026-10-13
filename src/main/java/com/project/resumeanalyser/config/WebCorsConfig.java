package com.project.resumeanalyser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Value("${app.redirect.target-url}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = new ArrayList<>(Arrays.asList(allowedOrigins.split(",")));

        // Include production domains
        origins.add("https://analyse.rajnishsystems.rajnshsystems.in");
        origins.add("https://analyse.rajnishsystems.in");

        // Include local development origins
        origins.add("http://localhost:5173");
        origins.add("http://localhost:3000");
        origins.add("http://127.0.0.1:5173");

        registry.addMapping("/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}