package com.project.resumeanalyser.controller;

import com.project.resumeanalyser.dto.FileDownloadDto;
import com.project.resumeanalyser.model.ResumeDocument;
import com.project.resumeanalyser.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ScoreMyCV Engine"));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ResumeDocument>> getAllUploadedResumes() {
        return ResponseEntity.ok(resumeService.getAllUploadedResumes());
    }

    @GetMapping("/view/{gridFsId}")
    public ResponseEntity<byte[]> viewResumePdf(@PathVariable String gridFsId) {
        try {
            FileDownloadDto fileDto = resumeService.getResumeFile(gridFsId);

            if (fileDto == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileDto.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileDto.filename() + "\"")
                    .body(fileDto.content());

        } catch (Exception e) {
            log.error("Failed to retrieve resume file with GridFS ID: {}", gridFsId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyseAts")
    public ResponseEntity<?> analyseAts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jd") String jobDescription) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Uploaded resume file cannot be empty."));
        }
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job description is required for ATS matching."));
        }

        try {
            Map<String, Object> response = resumeService.analyseAts(file, jobDescription);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error occurred while analyzing resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }
}