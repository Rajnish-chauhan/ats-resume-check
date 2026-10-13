package com.project.resumeanalyser.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.project.resumeanalyser.dto.FileDownloadDto;
import com.project.resumeanalyser.model.ResumeDocument;
import com.project.resumeanalyser.repo.ResumeRepository;
import org.apache.tika.Tika;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

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
            return null;
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

        // Extract full resume content using Apache Tika
        String resumeText = tika.parseToString(new ByteArrayInputStream(fileBytes));

        // Enforce safety cap on character length to handle multi-page resumes within model budget
        if (resumeText.length() > 20000) {
            resumeText = resumeText.substring(0, 20000);
        }

        // Objective corporate ATS evaluator instructions
        String systemInstructions = """
                You are a principal technical recruiter and corporate ATS (Applicant Tracking System) auditing engine for ScoreMyCV.
                Evaluate the candidate's resume strictly against the provided Job Description (JD).
                
                Provide an objective, highly actionable, and professional evaluation.
                
                You MUST return only a valid JSON object matching the exact schema below:
                {
                  "atsScore": <integer 0-100>,
                  "matchLevel": "<High Match | Moderate Match | Low Match>",
                  "scoreBreakdown": {
                    "skillsMatch": <integer 0-100>,
                    "experienceRelevance": <integer 0-100>,
                    "formattingAndStructure": <integer 0-100>
                  },
                  "executiveSummary": "<2-3 sentence executive evaluation summarizing strengths and critical alignment>",
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": [
                    { "skill": "skill name", "importance": "Critical | Preferred" }
                  ],
                  "criticalIssues": [
                    "<specific issue found, e.g., missing metrics, lack of cloud experience, poor section headers>"
                  ],
                  "actionableRecommendations": [
                    "<concrete step candidate must take to improve ATS pass rate and impact>"
                  ]
                }
                
                Do not include markdown code block formatting (such as ```json or ```). Return the raw JSON string only.
                """;

        String userPrompt = """
                RESUME CONTENT:
                %s
                
                TARGET JOB DESCRIPTION:
                %s
                """.formatted(resumeText, jobDescription);

        String aiResponse = chatClient.prompt()
                .system(systemInstructions)
                .user(userPrompt)
                .call()
                .content();

        // Clean any potential markdown wrapping
        String cleanJson = aiResponse.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        }
        if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        // Store file in GridFS
        ObjectId gridFsId = gridFsTemplate.store(
                new ByteArrayInputStream(fileBytes),
                originalFileName,
                contentType
        );

        // Save analysis document in MongoDB
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