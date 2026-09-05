package com.project.resumeanalyser.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.project.resumeanalyser.dto.FileDownloadDto;
import com.project.resumeanalyser.model.ResumeDocument;
import com.project.resumeanalyser.repo.ResumeRepository;
import org.apache.tika.Tika;
import org.bson.types.ObjectId;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ChatClient chatClient;
    private final Tika tika;
    private final ResumeRepository resumeRepository;
    private final GridFsTemplate gridFsTemplate;

    public ResumeServiceImpl(ChatModel chatModel, ResumeRepository resumeRepository, GridFsTemplate gridFsTemplate) {
        this.chatClient = ChatClient.create(chatModel);
        this.resumeRepository = resumeRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.tika = new Tika();
    }

    @Override
    public List<ResumeDocument> getAllUploadedResumes() {
        return resumeRepository.findAll();
    }

    @Override
    public FileDownloadDto getResumeFile(String gridFsId) throws Exception {
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(gridFsId)));

        if (gridFSFile == null) {
            return null; // The controller will handle sending the 404
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
        byte[] fileBytes = resource.getContentAsByteArray();

        String contentType = (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("_contentType") != null)
                ? gridFSFile.getMetadata().getString("_contentType")
                : MediaType.APPLICATION_PDF_VALUE;

        return new FileDownloadDto(fileBytes, contentType, gridFSFile.getFilename());
    }

    @Override
    public Map<String, Object> analyseAts(MultipartFile file, String jobDescription) throws Exception {
        byte[] fileBytes = file.getBytes();
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // 1. Extract Text
        String resumeText = tika.parseToString(new ByteArrayInputStream(fileBytes));
        if (resumeText.length() > 5000) {
            resumeText = resumeText.substring(0, 5000);
        }

        // 2. AI Prompt Generation
        String prompt = """
                You are an expert ATS (Applicant Tracking System) analyser. Compare the following resume with the Job Description.
                ----
                %s
                ----
                Job Description
                ----
                %s
                ----
                Return a structured JSON with exactly these keys:
                {"atsScore": 0, "matchedKeyword": [], "missingKeywords": [], "summary": "","issue":"","suggestion":""}
                
                Keep the response strictly valid JSON without markdown formatting.
                """.formatted(resumeText, jobDescription);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();

        // 3. Store File in GridFS
        ObjectId gridFsId = gridFsTemplate.store(
                new ByteArrayInputStream(fileBytes),
                originalFileName,
                contentType
        );

        // 4. Save to Repository
        ResumeDocument doc = new ResumeDocument();
        doc.setJobDescription(jobDescription);
        doc.setFileName(originalFileName);
        doc.setFileType(contentType);
        doc.setAiReport(cleanJson);
        doc.setGridFsFileId(gridFsId.toString());

        resumeRepository.save(doc);

        return Map.of("atsReport", cleanJson);
    }
}