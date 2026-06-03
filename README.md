                    # Resume Analyser

A high-performance web application that leverages artificial intelligence to provide real-time, actionable feedback on resumes based on specific job descriptions.

## Features

* [cite_start]**AI-Powered Analysis:** Integrated with the Google Gemini AI API to provide intelligent, contextual suggestions for resume improvements[cite: 15].
* [cite_start]**Real-time Processing:** Utilizes multi-threading and robust Exception Handling to ensure fast, efficient response generation[cite: 15].
* [cite_start]**Responsive UI:** A modern, clean, and professional interface built with React, JSX, and Tailwind CSS[cite: 16].
* [cite_start]**Keyword Optimization:** Automates the extraction of key skills from Job Descriptions (JD) and compares them against user resumes to suggest targeted optimizations[cite: 17].

## Tech Stack

**Frontend:**
* React.js
* JSX
* Tailwind CSS

**Backend:**
* Java
* Spring Boot
* Google Gemini AI API
* Exception Handling & Multithreading
* MongoDB

**Tools & Testing:**
* Postman (End-to-end API Testing)
* Git & GitHub (Version Control)

## How It Works

1.  **Input:** The user uploads their resume and pastes the Job Description (JD).
2.  **Processing:** The backend service securely communicates with the Gemini AI API to analyze the document content[cite: 15].
3.  **Optimization:** The system performs keyword extraction to identify missing competencies and delivers customized suggestions to the user[cite: 17].

## Getting Started

### Prerequisites
* JDK 21 or higher
* React.js & npm
* API Key for Google Gemini AI

### Backend Setup
1. Clone the repository.
2. Configure your `application.properties` with your Gemini AI API key.
3. Build the project using Maven:
   ```bash
   mvn clean install