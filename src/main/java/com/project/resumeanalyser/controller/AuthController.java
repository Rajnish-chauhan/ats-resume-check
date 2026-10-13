package com.project.resumeanalyser.controller;

import com.project.resumeanalyser.dto.LoginRequest;
import com.project.resumeanalyser.dto.LoginResponse;
import com.project.resumeanalyser.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
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