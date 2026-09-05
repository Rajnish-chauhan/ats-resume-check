package com.project.resumeanalyser.service;

import com.project.resumeanalyser.dto.FileDownloadDto;
import com.project.resumeanalyser.model.ResumeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResumeService {
    List<ResumeDocument> getAllUploadedResumes();
    FileDownloadDto getResumeFile(String gridFsId) throws Exception;
    Map<String, Object> analyseAts(MultipartFile file, String jobDescription) throws Exception;
}