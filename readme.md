# ScoreMyCV — Enterprise AI ATS Resume Benchmark


[![Java 21](https://img.shields.io/badge/JAVA-21+-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/) [![Spring Boot 3.X](https://img.shields.io/badge/SPRING%20BOOT-3.X-4cbf4c?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot) [![Spring AI OpenAI](https://img.shields.io/badge/SPRING%20AI-OPENAI-00A67E?style=for-the-badge&logo=openai&logoColor=white)](https://spring.io/projects/spring-ai)
[![MongoDB GridFS](https://img.shields.io/badge/MONGODB-GRIDFS-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![React 19](https://img.shields.io/badge/REACT-19+-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/) [![Tailwind CSS V4](https://img.shields.io/badge/TAILWIND%20CSS-V4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Docker Enabled](https://img.shields.io/badge/DOCKER-ENABLED-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/) [![AWS EC2 Deployed](https://img.shields.io/badge/AWS%20EC2-DEPLOYED-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/ec2/)

An enterprise-ready, full-stack ATS evaluation platform integrating **Apache Tika Document Ingestion**, **Spring AI with OpenAI (`gpt-4o-mini`)**, **MongoDB GridFS Binary Persistence**, and a **React 19 Executive Audit Dashboard**.

---

### Built With



## Live Deployments

- **Web Portal:** [ats.rajnishsystems.in](https://ats.rajnishsystems.in)
- **API Engine:** [analyse.rajnishsystems.in](https://analyse.rajnishsystems.in)
- **Health Check:** [analyse.rajnishsystems.in/api/resume/health](https://analyse.rajnishsystems.in/api/resume/health)

---

##  Core Features

- **Multi-Format Ingestion:** Extracts unstructured text from multi-page `.pdf`, `.doc`, and `.docx` resumes (up to 15MB) via Apache Tika.
- **Granular ATS Benchmarking:** Scores candidates across Skills Alignment, Experience Relevance, and Formatting (0–100 scale).
- **Deterministic AI Engine:** OpenAI `gpt-4o-mini` with strict JSON schema enforcement and low temperature (`0.2`) eliminates hallucinations.
- **Binary Persistence (GridFS):** Document files are chunked into MongoDB GridFS for inline PDF viewing and direct DOCX downloads.
- **Zero-Credential Frontend Auth:** Admin console (`/admin`) is protected via server-issued Bearer session tokens; zero passwords exist in client bundles.
- **Automated CI/CD:** GitHub Actions builds multi-stage Docker images and deploys zero-downtime updates to AWS EC2 over SSH.

---

##  System Architecture

```text
┌────────────────────────────────────────────────────────┐
│               Candidate & Admin Browser                │
└──────────────────────────┬─────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
┌─────────────────────────┐ ┌─────────────────────────┐
│   Vercel Edge Network   │ │    Cloudflare Proxy     │
│  ats.rajnishsystems.in  │ │analyse.rajnishsystems.in│
│   React 19 Client SPA   │ │ Origin Rewrite ➔ 8082   │
└─────────────────────────┘ └────────────┬────────────┘
                                         ▼
                            ┌─────────────────────────┐
                            │      AWS EC2 Server     │
                            │  Docker (Host Port 8082)│
                            │  Spring Boot 4.x / JRE21│
                            └────────────┬────────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
┌──────────────┐                 ┌──────────────┐                 ┌──────────────┐
│ Apache Tika  │                 │  Spring AI   │                 │MongoDB Atlas │
│ 2.9.2 Parser │                 │OpenAI gpt-4o │                 │GridFS Chunks │
│PDF, DOC, DOCX│                 │Strict JSON   │                 │Binary Stream │
└──────────────┘                 └──────────────┘                 └──────────────┘
```
## End-to-End Workflow
* Document Upload: Candidate uploads resume (.pdf, .doc, .docx) up to 15MB alongside target Job Description.
* Text Extraction: Apache Tika extracts unstructured text streams, stripping metadata and sanitizing to a 20k-character boundary.
* AI Semantic Audit: Spring AI prompts OpenAI gpt-4o-mini with strict JSON schema constraints at temperature = 0.2 to eliminate hallucinations.
* Dual Persistence: Raw binary bytes stream into MongoDB GridFS while structured audit reports save to ResumeDocument.
* Report Rendering: React renders granular score breakdown (Skills, Experience, Readability) and STAR-format improvement critique.

##  Quick Start (Local Setup)

### 1. Prerequisites
- **JDK 21+**
- **Node.js 18+** & npm
- **MongoDB** (Local on `localhost:27017` or Atlas URI)
- **OpenAI API Key**

### 2. Backend Setup
```bash
# Clone the repository
git clone [https://github.com/Rajnish-chauhan/ats-resume-check.git](https://github.com/Rajnish-chauhan/ats-resume-check.git)
cd ats-resume-check
```

# Set your OpenAI API Key
```text

export OPENAI_API_KEY="sk-proj-your-key"
```

# Build and run 
(Spring Boot starts on http://localhost:8080)
```text

mvn clean install
mvn spring-boot:run
```

### 3. Frontend Setup

```bash
cd frontend
npm install

# Start development server on http://localhost:5173
npm run dev
```

###  Deployment & CI/CD Pipeline

- **GitHub Actions Workflow:** Automatically triggers on every push to `main`.
- **Multi-Stage Build:** Generates optimized container image via `eclipse-temurin:21-jre-alpine`.
- **Remote Host Deployment:** Deploys container to AWS EC2 mapped to host port `8082`.
- **Edge Routing:** Cloudflare Origin Rule redirects `analyse.rajnishsystems.in` directly to port `8082`, coexisting with existing microservices on ports `80` and `8081` without Nginx.

### 👨‍💻 Author & Contact

**Rajnish Chauhan**  
*Backend Software Engineer | Java 21, Spring Boot, Microservices, Cloud Infrastructure*

- **Portfolio:** [rajnishsystems.in](https://rajnishsystems.in)
- **GitHub:** [@Rajnish-chauhan](https://github.com)
- **LinkedIn:** [linkedin.com/in/rajnish-chauhan](https://linkedin.com)
