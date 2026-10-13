package com.project.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final String adminUsername;
    private final String adminPassword;

    // Active session store: Token -> Expiry Timestamp (24 hours validity)
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_LIFETIME_MS = 24 * 60 * 60 * 1000L;

    public AuthService(
            @Value("${app.admin.username:${ADMIN_USERNAME}}") String adminUsername,
            @Value("${app.admin.password:${ADMIN_PASSWORD}}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public String authenticate(String username, String password) {
        if (username != null && password != null &&
                username.trim().equals(this.adminUsername.trim()) &&
                password.trim().equals(this.adminPassword.trim())) {

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