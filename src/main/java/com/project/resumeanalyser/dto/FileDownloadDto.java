package com.project.resumeanalyser.dto;

public record FileDownloadDto(byte[] content, String contentType, String filename) {
}