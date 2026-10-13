package com.project.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    // Session cache with 24-hour expiration
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_LIFETIME_MS = 24 * 60 * 60 * 1000L;

    public String authenticate(String username, String password) {
        if (username != null && password != null &&
                username.trim().equals(adminUsername.trim()) &&
                password.trim().equals(adminPassword.trim())) {

            String token = UUID.randomUUID().toString();
            activeSessions.put(token, System.currentTimeMillis() + SESSION_LIFETIME_MS);
            return token;
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Long expiryTimestamp = activeSessions.get(token);
        if (expiryTimestamp == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiryTimestamp) {
            activeSessions.remove(token);
            return false;
        }

        return true;
    }

    public void invalidateToken(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}