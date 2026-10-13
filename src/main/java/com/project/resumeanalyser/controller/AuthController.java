package com.project.resumeanalyser.controller;

import com.project.resumeanalyser.dto.LoginRequest;
import com.project.resumeanalyser.dto.LoginResponse;
import com.project.resumeanalyser.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(false, null, "Username and password cannot be empty."));
        }

        log.info("[AuthController] Login request received for user: {}", request.username());
        String token = authService.authenticate(request.username(), request.password());

        if (token != null) {
            return ResponseEntity.ok(new LoginResponse(true, token, "Authentication successful."));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Invalid username or password."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.invalidateToken(token);
        }
        return ResponseEntity.ok().build();
    }
}