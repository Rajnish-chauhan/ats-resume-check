package com.project.resumeanalyser.dto;

public record LoginResponse(boolean success, String token, String message) {
}