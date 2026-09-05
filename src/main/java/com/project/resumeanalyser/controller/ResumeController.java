package com.project.resumeanalyser.controller;

import com.project.resumeanalyser.dto.FileDownloadDto;
import com.project.resumeanalyser.model.ResumeDocument;
import com.project.resumeanalyser.service.ResumeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "${app.redirect.target-url}")
public class ResumeController {

    private final ResumeService resumeService;

    // Dependency Injection of the Service
    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
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
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyseAts")
    public ResponseEntity<Map<String, Object>> analyseAts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jd") String jobDescription) {
        try {
            Map<String, Object> response = resumeService.analyseAts(file, jobDescription);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Backend Error: " + e.getMessage()));
        }
    }
}