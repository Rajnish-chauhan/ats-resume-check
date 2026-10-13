package com.project.resumeanalyser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final String adminUsername;
    private final String adminPassword;

    // Active session store: Token -> Expiry Timestamp (24 hours validity)
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_LIFETIME_MS = 24 * 60 * 60 * 1000L;

    public AuthService(
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:admin123}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        log.info("[AuthService] Initialized with admin username: '{}'", this.adminUsername);
    }

    public String authenticate(String username, String password) {
        if (username == null || password == null) {
            log.warn("[AuthService] Login failed: null credentials supplied.");
            return null;
        }

        String inputUser = username.trim();
        String inputPass = password.trim();

        boolean userMatch = inputUser.equalsIgnoreCase(this.adminUsername.trim());
        boolean passMatch = inputPass.equals(this.adminPassword.trim());

        if (userMatch && passMatch) {
            String token = UUID.randomUUID().toString();
            activeSessions.put(token, System.currentTimeMillis() + SESSION_LIFETIME_MS);
            log.info("[AuthService] Login SUCCESS for user: '{}'", inputUser);
            return token;
        }

        log.warn("[AuthService] Login FAILED for user: '{}'. (User match: {}, Pass match: {})",
                inputUser, userMatch, passMatch);
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
            log.info("[AuthService] Session invalidated.");
        }
    }
}