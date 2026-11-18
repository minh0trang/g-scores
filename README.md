# G-Scores — Vietnam High School Graduation Exam 2024 Analytics & Lookup Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB.svg?logo=react)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38B2AC.svg?logo=tailwind-css)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg?logo=docker)](https://www.docker.com/)

> **G-Scores** is a high-performance web platform designed to ingest, process, and analyze the complete **2024 Vietnam National High School Graduation Exam dataset** (1,061,605 candidates, 6,037,435 exam scores). The system delivers instantaneous score lookup, candidate rankings across 10 college admission blocks (nationwide and province-wide), dynamic score tier distribution analytics, and Top 10 leaderboards.

---

## 🚀 Key Performance Milestones & Technical Highlights

| Metric / Feature | Benchmark / Implementation Detail |
| :--- | :--- |
| **Dataset Ingestion** | **1,061,605 candidates** ingested in **< 60 seconds** using JDBC batching (`reWriteBatchedInserts=true`), 5,000-row chunks, and unlogged table optimizations. |
| **Admission Block Precompute** | **2,627,815 block scores** (A00, A01, B00, C00, D01–D07) aggregated and indexed via a single fast SQL pivot immediately following data seeding. |
| **Zero N+1 Query Traversal** | **Single correlated query** computing both National and Provincial rankings for all blocks of a student in **~1–2ms** using Composite Indexes (`Index-Only Scan`, 0 Heap Fetches). |
| **In-Memory Caching** | O(1) memory lookup for national and provincial totals per block (`BlockTotalCache`), completely avoiding redundant `COUNT(*)` queries on immutable exam data. |
| **Competition Ranking** | Standard Competition Rank (1, 2, 2, 4) applied uniformly across Top 10 leaderboards and individual candidate block rankings. |
| **Responsive SPA UI** | Pure Single-Page Application with 0 layout shift (`scrollbar-gutter: stable`), non-scrolling single viewport design, and instant search feedback. |

---

## 🏛️ System Architecture

G-Scores follows a **Modular Monolith** architecture with strict layer decoupling, comprehensive domain modeling, and automated database migrations managed by **Flyway**.

```
g-scores/
├── backend/
│   ├── src/main/java/com/example/backend/
│   │   ├── common/             # Global exceptions, CORS, constants, base responses
│   │   ├── config/             # Caches, HikariCP, JPA configuration
│   │   ├── domain/             # Entities: Student, ExamScore, Province, StudentBlockScore
│   │   ├── dto/                # Request / Response Transfer Objects & Projections
│   │   ├── repository/         # Spring Data JPA & native high-throughput repositories
│   │   ├── service/            # Business logic: StudentService, ReportService, Seeder
│   │   └── controller/         # REST Controllers (Student, Report, Province)
│   └── src/main/resources/
│       ├── db/migration/       # Flyway SQL migrations (V1 to V4)
│       └── application.properties
├── frontend/
│   ├── src/
│   │   ├── components/         # Reusable UI widgets, charts, selectors, modals
│   │   ├── hooks/              # Custom React hooks (useScoreLookup, useReportData)
│   │   ├── pages/              # Dashboard, ScoreLookup, Leaderboard
│   │   └── services/           # Axios HTTP client & G-Scores API service
│   ├── nginx.conf              # Production Nginx reverse proxy configuration
│   └── Dockerfile              # Multi-stage production build
├── dataset/                    # Exam CSV dataset mount point
├── docker-compose.yml          # Production multi-container orchestration
└── README.md
```

### Database Evolution (Flyway)
1. **`V1__init_schema.sql`**: Core relational model — `students` (VARCHAR(15) SBD, language code) and `exam_scores` (EAV design with CHECK constraints for valid score ranges 0.00–10.00).
2. **`V2__add_indexes.sql`**: Composite indexes for fast student and subject lookups (`idx_exam_scores_subject_score`).
3. **`V3__add_provinces.sql`**: Normalized `provinces` table (63 provinces/cities) and candidate province association via 2-digit SBD prefix.
4. **`V4__create_student_block_scores.sql`**: High-performance flat table `student_block_scores` storing precomputed total scores for A00, A01, B00, C00, D01–D07, backed by multi-column indexes (`(block_code, total_score DESC)` and `(block_code, province_code, total_score DESC)`).

---

## 🧠 Object-Oriented Domain Modeling (OOP Design)

### 1. Subject & Score Classification Strategy
All 9 official exam subjects are modeled with polymorphic metadata, score validation, and 4-tier grade classification (`EXCELLENT` $\ge 8.0$, `GOOD` $\ge 6.0$, `AVERAGE` $\ge 4.0$, `LOW` $< 4.0$):
* `TOAN` (Math), `NGU_VAN` (Literature), `NGOAI_NGU` (Foreign Language)
* Natural Sciences: `VAT_LI` (Physics), `HOA_HOC` (Chemistry), `SINH_HOC` (Biology)
* Social Sciences: `LICH_SU` (History), `DIA_LI` (Geography), `GIAO_DUC_CONG_DAN` (Civics)

### 2. MOET Foreign Language & Admission Block Rules
* **Block A00**: Toán + Vật lí + Hóa học.
* **Block A01**: Toán + Vật lí + Tiếng Anh (strictly requires foreign language code `N1`).
* **Block B00**: Toán + Hóa học + Sinh học.
* **Block C00**: Ngữ văn + Lịch sử + Địa lí.
* **Blocks D01 – D07**: Toán + Ngữ văn + Ngoại ngữ (mapped strictly by registered language code: `N1` $\rightarrow$ D01, `N2` $\rightarrow$ D02, ..., `N7` $\rightarrow$ D07). Candidates are ranked only within peers of the same language block.

---

## 🛠️ Quick Start with Docker (Recommended)

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) (v20.10+ and Docker Compose v2+)
* Ensure the dataset CSV file is placed at `dataset/diem_thi_thpt_2024.csv`.

### One-Command Deployment
```bash
# Clone the repository
git clone https://github.com/your-username/g-scores.git
cd g-scores

# Build and start all 3 services (PostgreSQL, Spring Boot Backend, Nginx Frontend)
docker compose up --build
```

### What Happens Automatically:
1. **PostgreSQL** starts and initializes a healthy database instance.
2. **Spring Boot Backend** starts, executes Flyway migrations (`V1` $\rightarrow$ `V4`), seeds 1,061,605 student records, aggregates 2.62M block scores, warms up in-memory caches, and passes healthcheck.
3. **Nginx Frontend** starts on port 80 and serves the Single-Page Application with reverse-proxy routing to `/api/`.

### Access Points
* **Web Application**: [http://localhost](http://localhost)
* **Backend API**: [http://localhost/api/v1](http://localhost/api/v1) (or direct port [http://localhost:8080/api/v1](http://localhost:8080/api/v1))
* **Health Check**: [http://localhost:8080/api/v1/provinces](http://localhost:8080/api/v1/provinces)

To stop the containers:
```bash
docker compose down
```

---

## 💻 Manual Local Development Setup

If you wish to run services locally outside Docker:

### 1. Start PostgreSQL
```bash
docker compose up -d postgres
```

### 2. Run Backend (Java 21, Spring Boot)
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run
# Linux / macOS:
./mvnw spring-boot:run
```

### 3. Run Frontend (Node 20, Vite)
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 📡 RESTful API Documentation

All endpoints return standardized JSON responses:

### 1. Candidate Score & Block Ranking Lookup
* **Endpoint**: `GET /api/v1/students/{sbd}/scores`
* **Path Variable**: `sbd` — 8-digit Candidate Registration Number (e.g. `01000001`).
* **Response (200 OK)**:
```json
{
  "code": 200,
  "message": "Tra cứu điểm thi thành công",
  "data": {
    "sbd": "01000001",
    "provinceCode": "01",
    "provinceName": "Hà Nội",
    "languageCode": "N1",
    "scores": {
      "TOAN": 8.0,
      "NGU_VAN": 7.75,
      "NGOAI_NGU": 8.6,
      "VAT_LI": 7.25,
      "HOA_HOC": 6.5,
      "SINH_HOC": 5.75
    },
    "blocks": [
      {
        "blockCode": "A00",
        "blockName": "Khối A00 (Toán – Lý – Hóa)",
        "totalScore": 21.75,
        "nationalRank": 85124,
        "nationalTotal": 325410,
        "provincialRank": 9412,
        "provincialTotal": 38120
      },
      {
        "blockCode": "D01",
        "blockName": "Khối D01 (Toán – Văn – Anh)",
        "totalScore": 24.35,
        "nationalRank": 14210,
        "nationalTotal": 540120,
        "provincialRank": 1620,
        "provincialTotal": 62450
      }
    ]
  }
}
```

### 2. Top 10 Group A00 Leaderboard
* **Endpoint**: `GET /api/v1/reports/group-a-top10`
* **Query Parameter**: `province` (optional) — 2-digit province code (e.g., `01` for Hà Nội). Omit to query nationwide.
* **Response (200 OK)**: Top 10 candidates ordered by total score DESC and SBD ASC with Competition Rank (1, 2, 2, 4).

### 3. Subject Score Distribution
* **Endpoint**: `GET /api/v1/reports/distribution`
* **Query Parameter**: `province` (optional) — 2-digit province code.
* **Response (200 OK)**: Aggregate statistics for all 9 subjects broken down into 4 tiers (`excellent`, `good`, `average`, `low`).

### 4. Province List
* **Endpoint**: `GET /api/v1/provinces`
* **Response (200 OK)**: Complete list of 63 Vietnamese provinces/cities with their 2-digit code and official name.

---

## 🧪 Testing & Verification

* **Backend Unit & Integration Tests**:
  ```bash
  cd backend
  ./mvnw test
  ```
* **Frontend ESLint & Production Bundle**:
  ```bash
  cd frontend
  npm run lint
  npm run build
  ```

---

## 🌐 Live Demo & Deployment

* **Live Demo URL**: [https://g-scores.example.com](https://g-scores.example.com) *(Update with your production deployment link)*
* **API Base URL**: `https://g-scores.example.com/api/v1`

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).

