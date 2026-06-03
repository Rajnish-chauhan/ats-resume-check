package com.project.resumeanalyser.repo;

import com.project.resumeanalyser.model.ResumeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeRepository extends MongoRepository<ResumeDocument, String> {
}