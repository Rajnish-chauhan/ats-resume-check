package com.project.resumeanalyser.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://ats.rajnishsystems.in"})
public class ResumeController {

    private final ChatClient chatClient;
    private final Tika tika = new Tika();
    private final ResumeRepository resumeRepository;

    // 1. Inject GridFsTemplate
    private final GridFsTemplate gridFsTemplate;

    public ResumeController(ChatModel chatModel, ResumeRepository resumeRepository, GridFsTemplate gridFsTemplate) {
        this.chatClient = ChatClient.create(chatModel);
        this.resumeRepository = resumeRepository;
        this.gridFsTemplate = gridFsTemplate;
    }
    @GetMapping("/all")
    public List<ResumeDocument> getAllUploadedResumes() {
        // Fetch all documents from the 'analyzed_resumes' collection
        return resumeRepository.findAll();
    }

    // ENDPOINT 2: Stream the actual PDF to the screen
    @GetMapping("/view/{gridFsId}")
    public ResponseEntity<byte[]> viewResumePdf(@PathVariable String gridFsId) {
        try {
            // Find the file chunks in GridFS using the ID
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    new Query(Criteria.where("_id").is(gridFsId))
            );

            if (gridFSFile == null) {
                return ResponseEntity.notFound().build();
            }

            // Reconstruct the physical bytes from the chunks
            GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
            byte[] fileBytes = resource.getContentAsByteArray();

            // Determine content type (default to PDF if unknown)
            String contentType = gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("_contentType") != null
                    ? gridFSFile.getMetadata().getString("_contentType")
                    : MediaType.APPLICATION_PDF_VALUE;

            // Send it back to the browser to be viewed inline
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + gridFSFile.getFilename() + "\"")
                    .body(fileBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyseAts")
    public Map<String, Object> analyseAts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jd") String jobDescription) {

        try {
            // Keep the bytes safe early
            byte[] fileBytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            // Extract text
            String resumeText = tika.parseToString(new ByteArrayInputStream(fileBytes));

            if (resumeText.length() > 5000) {
                resumeText = resumeText.substring(0, 5000);
            }

            String prompt = """
                    You are an expert ATS (Applicant Tracking System) analyser. Compare the following resume with the Job Description.
                    ----
                    %s
                    ----
                    Job Description: 
                    %s
                    ----
                    Return a structured JSON with exactly these keys:
                    {"atsScore": 0, "matchedKeyword": [], "missingKeywords": [], "summary": "","issue":"","suggestion":""}
                    
                    Keep the response strictly valid JSON without markdown formatting.
                    """.formatted(resumeText, jobDescription);

            // AI Call
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();

            // --- THE FIX: SAVE FILE TO GRIDFS INSTEAD OF STANDARD COLLECTION ---

            // 1. Store the actual file bytes safely via GridFS
            ObjectId gridFsId = gridFsTemplate.store(
                    new ByteArrayInputStream(fileBytes),
                    originalFileName,
                    contentType
            );

            // 2. Save the metadata and link the GridFS ID
            ResumeDocument doc = new ResumeDocument();
            doc.setJobDescription(jobDescription);
            doc.setFileName(originalFileName);
            doc.setFileType(contentType);
            doc.setAiReport(cleanJson);
            doc.setGridFsFileId(gridFsId.toString()); // Save the reference!

            resumeRepository.save(doc);

            return Map.of("atsReport", cleanJson);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Backend Error: " + e.getMessage());
        }
    }
}