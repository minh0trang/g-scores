# G-Scores — Vietnam High School Graduation Exam 2024 Analytics & Lookup Platform

[![Java](https://img.shields.io/badge/Java-17%20(Runtime%2021)-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20%2F%2018-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19.3-61DAFB.svg?logo=react)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38B2AC.svg?logo=tailwind-css)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED.svg?logo=docker)](https://www.docker.com/)

> **G-Scores** is a full-stack web platform designed to ingest, verify, and analyze the complete **2024 Vietnam National High School Graduation Exam dataset** (`1,061,605` candidates, `6,037,435` individual exam scores, `2,627,815` precomputed admission block records). The application provides candidate score lookup, nationwide and provincial rankings across **11 college admission blocks** (`A00, A01, B00, C00, D01–D07`), 4-tier score distribution charts, and Top 10 Group A00 leaderboards.

---

## 🌐 Live Demo & Repository

* **Live Web Application (Vercel)**: [https://g-scores-ten.vercel.app/](https://g-scores-ten.vercel.app/)
* **Production Backend API (Railway)**: [https://g-scores-production-6d4cc9f7.up.railway.app/api/v1](https://g-scores-production-6d4cc9f7.up.railway.app/api/v1)
* **GitHub Repository**: [https://github.com/minh0trang/g-scores.git](https://github.com/minh0trang/g-scores.git)

---

## 📊 Verified Performance Benchmarks

All query timings below were measured using PostgreSQL `EXPLAIN ANALYZE` on the local **PostgreSQL 16** dataset (`1,061,605` rows in `students`, `6,037,435` rows in `exam_scores`, `2,627,815` rows in `student_block_scores`, total DB size `~1.61 GB`):

| Operation / Query | Execution Plan | Measured Execution Time | Verification SQL |
| :--- | :--- | :---: | :--- |
| **Student Lookup by SBD** (`01000001`) | `Index Scan using uq_students_sbd` | **0.78 ms** | `EXPLAIN ANALYZE SELECT * FROM students WHERE sbd = '01000001';` |
| **Student Subject Scores** (`JOIN FETCH`) | `Index Scan` + `Hash Join` | **0.12 ms** | `EXPLAIN ANALYZE SELECT es.*, s.* FROM exam_scores es JOIN subjects s ON s.id = es.subject_id WHERE es.student_id = 1;` |
| **Block Ranks (All Blocks for Student)** | `Index Scan` + 2× `Index Only Scan` (`Heap Fetches: 0`) | **215.77 ms** *(mid-tier student scanning 184k index entries × 4 blocks; faster for top scorers)* | `StudentBlockScoreRepository.findBlockRanksByStudentId(1)` |
| **Top 10 Group A00 (Nationwide)** | `Parallel Index Scan using idx_block_score_nat` + `Limit 10` | **18.01 ms** | `ExamScoreRepository.findTop10GroupAFromBlockScores(NULL)` |
| **Top 10 Group A00 (Province `'01'`)** | `Index Scan using idx_block_score_prov` + `Limit 10` | **9.88 ms** | `ExamScoreRepository.findTop10GroupAFromBlockScores('01')` |
| **4-Tier Distribution (Nationwide, Cold)** | `Seq Scan on exam_scores` (6.03M rows) + `Hash Right Join` | **4,765.89 ms** *(warm in-memory cache: `< 1 ms`)* | `ExamScoreRepository.aggregateScoreDistributionBySubject()` |
| **4-Tier Distribution (Province `'01'`, Cold)** | `Parallel Seq Scan` + `Parallel Hash Join` | **736.76 ms** *(warm in-memory cache: `< 1 ms`)* | `ExamScoreRepository.aggregateScoreDistributionBySubjectAndProvince('01')` |

### Data Integrity, Readiness & Limitations
* **Streaming CSV Importer (`CsvDataSeeder`)**: Streams `diem_thi_thpt_2024.csv` via `BufferedReader` + `DigestInputStream` (computes `SHA-256` in a single pass with `O(batchSize)` memory), inserts in `5,000`-row transactional JDBC batches (`TransactionTemplate` + `reWriteBatchedInserts=true`), syncs PostgreSQL sequences, precomputes `student_block_scores` via a single SQL aggregation pass, runs `ANALYZE` safely on application tables, and persists verification state to `import_metadata`.
* **Readiness Gate & Atomic Epoch Guard**: All dataset-dependent endpoints (`/api/v1/reports/*` and `/api/v1/students/{sbd}/scores`) enforce atomic `(SeedState, epoch)` Entry and Exit checks in `DataSeedState`, returning **HTTP `503 Service Unavailable`** during seeding, unverified legacy state, incomplete data, or count mismatch — preventing partial reads and stale cache writes.
* **Known Limitation of Count Consistency Check**: On startup when `import_metadata.status = 'COMPLETED'`, `CsvDataSeeder` verifies that live row counts of `students`, `exam_scores`, and `student_block_scores` match `import_metadata` (`> 0`). While this reliably detects truncated/partial imports, missing rows, or table loss, it cannot detect silent in-place row value edits (`UPDATE`) that leave row counts unchanged without adding a full-table content hash.

---

## 🏛️ System Architecture

G-Scores is organized as a **Package-by-Feature Modular Monolith**:

```text
g-scores/
├── backend/
│   ├── src/main/java/com/example/backend/
│   │   ├── BackendApplication.java
│   │   ├── common/                 # ApiErrorResponse, BadRequestException, ResourceNotFoundException,
│   │   │                           # ServiceUnavailableException, GlobalExceptionHandler, WebCorsConfig
│   │   ├── importer/               # CsvDataSeeder, CsvRowParser, ImporterProperties, ParsedStudentRow,
│   │   │                           # SeedState, DataSeedState, DataSeederHealthIndicator
│   │   ├── province/               # Province, ProvinceRepository, ProvinceCache, ProvinceController, DTO
│   │   ├── report/                 # ReportController, ReportService, DTOs
│   │   ├── score/                  # ExamScore, ExamScoreRepository
│   │   ├── student/                # Student, StudentBlockScore, Repositories, BlockTotalCache,
│   │   │                           # StudentScoreService, StudentScoreController, DTOs
│   │   └── subject/                # OOP Subject hierarchy: ExamSubject, StandardExamSubject,
│   │                               # ForeignLanguageSubject, LanguageCode, ScoreTier, SubjectRegistry, Entity
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   │   ├── V1__init_schema.sql
│   │   │   ├── V2__seed_subjects.sql
│   │   │   ├── V3__add_provinces.sql
│   │   │   ├── V4__create_student_block_scores.sql
│   │   │   ├── V5__fix_province_names.sql
│   │   │   └── V6__create_import_metadata.sql
│   │   └── application.properties
│   ├── Dockerfile                  # Maven + Temurin JDK 21 build -> Temurin JRE 21 Alpine runtime
│   └── pom.xml                     # Spring Boot 4.1.2, Java 17 target
├── frontend/
│   ├── src/
│   │   ├── components/             # BlockScoreTable, GroupALeaderboardTable, MainLayout, ProvinceSelector,
│   │   │                           # SbdSearchForm, ScoreDistributionChart, SubjectScoreTable,
│   │   │                           # SubjectSelectorBar, SubjectTierSummary, TopThreeGroupACards
│   │   ├── hooks/                  # useReportData, useScoreLookup
│   │   ├── pages/                  # Dashboard, ScoreLookup, Leaderboard
│   │   ├── services/               # apiClient, gscoresService, mockData
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── nginx.conf                  # Production SPA routing + /api/ reverse proxy
│   ├── vercel.json                 # Vercel SPA fallback rewrite
│   └── Dockerfile                  # Node 20 Alpine build -> Nginx 1.27 Alpine runtime
├── dataset/                        # Place diem_thi_thpt_2024.csv here (gitignored)
├── docs/
│   └── TECHNICAL_AUDIT_REPORT.md   # Historical baseline technical audit snapshot
├── docker-compose.yml              # PostgreSQL 16 + Spring Boot Backend + Nginx Frontend
└── README.md
```

### Database Schema Evolution (Flyway `V1`–`V6`)
1. **`V1__init_schema.sql`**: Normalized schema (`students`, `subjects`, `exam_scores`) with `NUMERIC(4,2)` scores, `CHECK (score >= 0.00 AND score <= 10.00)`, unique constraints, and lookup indexes. Untaken subjects are omitted (**sparse storage**).
2. **`V2__seed_subjects.sql`**: Seeds the 9 official subjects (`TOAN`, `NGU_VAN`, `NGOAI_NGU`, `VAT_LI`, `HOA_HOC`, `SINH_HOC`, `LICH_SU`, `DIA_LI`, `GDCD`).
3. **`V3__add_provinces.sql`**: Creates `provinces`, adds `students.province_code` (2-digit SBD prefix) with FK and index.
4. **`V4__create_student_block_scores.sql`**: Creates `student_block_scores` (`A00`, `A01`, `B00`, `C00`, `D01`–`D07`) with `total_score NUMERIC(5,2)` and composite ranking indexes (`idx_block_score_nat`, `idx_block_score_prov`).
5. **`V5__fix_province_names.sql`**: Idempotently normalizes all **63 MOET Exam Council province names** (Công văn `1277/BGDĐT-QLCL`; code `20` unused; preserves 2-digit codes and FKs).
6. **`V6__create_import_metadata.sql`**: Creates `import_metadata` to track dataset completeness (`IN_PROGRESS`, `COMPLETED`, `FAILED`, `UNVERIFIED_LEGACY`) without hardcoding expected candidate counts.

---

## 🧠 Object-Oriented Domain Design (`subject` & Admission Rules)

* **`ExamSubject` (Abstract Base Class)** encapsulates immutable subject metadata (`code`, `name`, `csvColumnName`, `displayOrder`), score range validation (`isValidScore`), normalization (`normalizeScore`), tier classification (`classifyTier` via `ScoreTier`), and polymorphic display formatting (`formatDisplayName`).
* **`StandardExamSubject`** handles the 8 standard subjects; **`ForeignLanguageSubject`** overrides `formatDisplayName(String studentLanguageCode)` using `LanguageCode` (`N1` Tiếng Anh through `N7` Tiếng Hàn) to render e.g. `"Ngoại ngữ (N1 - Tiếng Anh)"`.
* **`SubjectRegistry`** manages all 9 subjects as an immutable Spring registry injected across `CsvRowParser`, `StudentScoreService`, and `ReportService`.
* **11 Precomputed Admission Blocks**: `A00` (Toán, Lý, Hóa), `A01` (Toán, Lý, Tiếng Anh `N1` only), `B00` (Toán, Hóa, Sinh), `C00` (Văn, Sử, Địa), and `D01`–`D07` (Toán, Văn, Ngoại ngữ `N1`–`N7` mapped per candidate's `language_code`). Rankings use **Competition Rank** (`1, 2, 2, 4`).

---

## ⚙️ Environment Variables

### Backend (`backend/src/main/resources/application.properties`)
| Variable | Default | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | HTTP port of the Spring Boot server |
| `DB_URL` | `jdbc:postgresql://localhost:5432/gscores?reWriteBatchedInserts=true` | PostgreSQL JDBC connection URI |
| `DB_USERNAME` | `gscores` | PostgreSQL username *(override with strong credentials in production)* |
| `DB_PASSWORD` | `gscores` | PostgreSQL password *(override with strong credentials in production)* |
| `IMPORTER_ENABLED` | `true` | Enable CSV seeding / verification on startup (`false` for read-only cloud containers without CSV) |
| `CSV_FILE_PATH` | `../dataset/diem_thi_thpt_2024.csv` | Path to the raw CSV dataset |
| `IMPORTER_BATCH_SIZE` | `5000` | JDBC batch flush size during CSV import (minimum `500`) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173,http://localhost:80,http://localhost` | Comma-separated allowed CORS origins (supports `*`) |

### Frontend (`frontend/.env` / Docker build args)
| Variable | Default | Description |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` (Docker build arg: `/api/v1`) | Base URL for Axios REST client |
| `VITE_USE_MOCK_API` | `false` | Set `'true'` to run UI against local `mockData.js` without backend |

---

## 🛠️ Running Locally with Docker Compose (Recommended)

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) (v20.10+ & Docker Compose v2+)
* Place the dataset CSV file at **`dataset/diem_thi_thpt_2024.csv`**.

### Start All Services
```bash
git clone https://github.com/minh0trang/g-scores.git
cd g-scores
docker compose up --build
```
1. `postgres` starts PostgreSQL 16 and passes `pg_isready`.
2. `backend` runs Flyway `V1`–`V6`, imports & verifies `dataset/diem_thi_thpt_2024.csv`, precomputes `student_block_scores`, warms caches, and exposes `200 UP` on `/actuator/health/readiness`.
3. `frontend` waits for backend readiness and serves the Nginx SPA on `http://localhost` (proxying `/api/` internally to `backend:8080`).

### Manual Local Development Setup
```bash
# 1. Start PostgreSQL container
docker compose up -d postgres

# 2. Run Backend (Java 17/21, Maven Wrapper)
cd backend
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # Linux / macOS

# 3. Run Frontend (Node 20.19+ / 22+, Vite)
cd ../frontend
npm install
npm run dev
```

---

## 📡 RESTful API & Health Probes Reference

When the dataset is **not ready** (`NOT_STARTED`, `SEEDING`, `NOT_READY_*`, `DATA_INCONSISTENT`, `FAILED`), all dataset-dependent endpoints below return **HTTP `503 Service Unavailable`** with `ApiErrorResponse`:
```json
{
  "status": 503,
  "message": "Dữ liệu điểm thi đang được khởi tạo hoặc chưa sẵn sàng (SEEDING). Vui lòng thử lại sau.",
  "timestamp": "2026-11-19T07:00:00Z"
}
```

### 1. Lookup Student Scores & Block Rankings
* **Endpoint**: `GET /api/v1/students/{sbd}/scores` (returns `400` if `sbd` is not 8 digits, `404` if not found)
* **Sample Response (`200 OK`)**:
```json
{
  "sbd": "26020938",
  "provinceCode": "26",
  "provinceName": "Thái Bình",
  "languageCode": "N1",
  "languageDisplayName": "Ngoại ngữ (N1 - Tiếng Anh)",
  "scores": {
    "TOAN": 9.60,
    "NGU_VAN": 8.00,
    "NGOAI_NGU": 9.60,
    "VAT_LI": 10.00,
    "HOA_HOC": 10.00,
    "SINH_HOC": 8.75,
    "LICH_SU": null,
    "DIA_LI": null,
    "GDCD": null
  },
  "blocks": [
    {
      "blockCode": "A00",
      "blockName": "Toán, Vật lí, Hóa học",
      "totalScore": 29.60,
      "nationalRank": 1,
      "nationalTotal": 343800,
      "provincialRank": 1,
      "provincialTotal": 9064
    }
  ]
}
```

### 2. Overview Statistics
* **Endpoint**: `GET /api/v1/reports/overview` *(optional query `?province=<code>`, returns `400` on invalid province code)*
* **Response (`200 OK`)**:
```json
{
  "totalStudents": 1061605,
  "totalScores": 6037435,
  "totalSubjects": 9,
  "topGroupAScore": 29.60
}
```

### 3. Subject 4-Tier Score Distribution
* **Endpoint**: `GET /api/v1/reports/score-distribution` *(optional query `?province=<code>`)*
* **Response (`200 OK`)**: JSON array of 9 objects (`subjectCode`, `subjectName`, `excellent`, `good`, `average`, `low`).

### 4. Top 10 Group A00 Leaderboard
* **Endpoint**: `GET /api/v1/reports/group-a/top-10` *(optional query `?province=<code>`)*
* **Response (`200 OK`)**: JSON array of up to 10 objects (`rank`, `sbd`, `provinceCode`, `provinceName`, `toan`, `vatLi`, `hoaHoc`, `totalScore`, `languageCode`) ordered by `totalScore DESC, sbd ASC` using Competition Rank.

### 5. Official Provinces List
* **Endpoint**: `GET /api/v1/provinces`
* **Response (`200 OK`)**: JSON array of all 63 `{ "code": "01", "name": "Hà Nội" }` entries sorted by `code ASC` (static reference data; always returns `200 OK` when DB is up).

### 6. Actuator Health Probes (Spring Boot `4.1.2`)
* **`GET /actuator/health/liveness`**: Checks `livenessState` only (`200 {"status":"UP"}` whenever JVM is alive).
* **`GET /actuator/health/readiness`**: Checks `readinessState, db, dataSeeder` (`200 {"status":"UP"}` only after dataset completeness verification succeeds; otherwise `503`).
* **`GET /actuator/health`**: Aggregates all indicators including `dataSeeder`. On Railway, configure Healthcheck Path = **`/actuator/health/readiness`**.

---

## 🚀 Railway / Aiven Production Deployment Runbook

When deploying a build containing `V5` + `V6` against a **legacy cloud database (PostgreSQL 18 on Aiven)** where the Railway container does **not** mount `diem_thi_thpt_2024.csv`:

> **Safety Invariants**:
> * NEVER mark `import_metadata.status = 'COMPLETED'` manually via SQL based on row counts alone.
> * NEVER let an older Railway backend serve requests while its target database is being truncated, dropped, or restored.
> * Always perform and verify backups using **PostgreSQL 18** tooling (`postgres:18-alpine`) matching Aiven's server version.

### Step 0 — Mandatory Two-Layer Backup & Local PG 18 Restore Verification (Hard Gate)
1. **Aiven Managed Snapshot**: Confirm the latest snapshot / Backup ID in the Aiven Console.
2. **Logical Backup with PostgreSQL 18**:
   ```bash
   docker run --rm postgres:18-alpine pg_dump -Fc --no-owner --no-acl "<AIVEN_URI>" > aiven_pre_v6_backup.dump
   ```
3. **Mandatory Ephemeral PostgreSQL 18 Restore Verification** (must pass 100% before touching Aiven):
   ```bash
   docker run -d --name gscores-pg18-verify -e POSTGRES_USER=verify -e POSTGRES_PASSWORD=verify -e POSTGRES_DB=postgres postgres:18-alpine
   docker exec -i gscores-pg18-verify pg_restore --list < aiven_pre_v6_backup.dump | grep -E "TABLE DATA|SEQUENCE|INDEX|CONSTRAINT"
   docker exec gscores-pg18-verify psql -U verify -d postgres -c "DROP DATABASE IF EXISTS gscores_backup_verify;"
   docker exec gscores-pg18-verify psql -U verify -d postgres -c "CREATE DATABASE gscores_backup_verify;"
   docker exec -i gscores-pg18-verify pg_restore -U verify -d gscores_backup_verify --no-owner --no-acl --exit-on-error < aiven_pre_v6_backup.dump
   ```
   Verify all 5 checks against the live Aiven baseline before proceeding: (1) tables & `flyway_schema_history` (`V1`–`V4`), (2) sequence `last_value >= MAX(id)`, (3) all indexes on `pg_indexes`, (4) all PK/UQ/FK/CHECK constraints on `pg_constraint`, and (5) exact table row counts.

### Step 1 — Produce Verified Local Export via `CsvDataSeeder`
Run the new backend locally against `gscores-postgres` with `IMPORTER_ENABLED=true` and `dataset/diem_thi_thpt_2024.csv` so Flyway applies `V5` + `V6` and `CsvDataSeeder` completes a clean CSV re-import (`import_metadata.status = 'COMPLETED'`, SHA-256 recorded). Restart once to confirm `READY_EXISTING_DATA`, then dump using a PG 16/18 compatible custom archive:
```bash
docker exec gscores-postgres pg_dump -U gscores -d gscores -Fc --no-owner --no-acl > local_verified_v6.dump
```

### Method Risk Comparison
| Criterion | Restore `local_verified_v6.dump` (`pg_restore`) | Direct WAN re-import (`CsvDataSeeder` -> Aiven) |
| :--- | :--- | :--- |
| **WAN Failure Risk** | Low–Medium (~2–4 min stream) | High (~5–15+ min over WAN; partial batches if socket drops) |
| **If Old Railway Is Active** | Critical (`DROP`/`--clean` breaks live queries) | Critical (`TRUNCATE` clears live tables & poisons old cache) |
| **Recommendation** | **Preferred** (fast, deterministic, verified checksum) | Fallback only if `pg_restore` is unavailable |

### Step 2 — Cutover Strategy
* **Option 1 (Preferred — Isolated Target Database `gscores_v2`)**:
  1. Keep `defaultdb` and old Railway untouched serving live traffic.
  2. Create `gscores_v2` on Aiven (`CREATE DATABASE gscores_v2;`) and restore `local_verified_v6.dump` into `gscores_v2` via `postgres:18-alpine`, then run `ANALYZE students; ANALYZE exam_scores; ANALYZE student_block_scores;`.
  3. Verify `V1`–`V6`, `import_metadata.status = 'COMPLETED'`, SHA-256 checksum, 3-table counts, and 63 provinces on `gscores_v2`.
  4. Update Railway `DB_URL` to `gscores_v2`, `IMPORTER_ENABLED=false`, Healthcheck Path = `/actuator/health/readiness`, and deploy new Backend + Vercel Frontend (`frontend/vercel.json`). Keep `defaultdb` for 24h as fallback.
* **Option 2 (Maintenance Window on `defaultdb` when single-DB constrained)**:
  1. Stop/scale down Railway Backend first and confirm zero active app connections in `pg_stat_activity`.
  2. Cleanly drop/replace `defaultdb` using `postgres:18-alpine` `pg_restore`, run `ANALYZE` on all 3 data tables, and verify `V1`–`V6` + `import_metadata` + counts + provinces.
  3. Set Railway `IMPORTER_ENABLED=false`, Healthcheck Path = `/actuator/health/readiness`, deploy new Backend + Vercel Frontend, and wait for `/actuator/health/readiness` `200 UP`.

### Step 3 — Failure Rollback Procedures
* **Option 1 Rollback**: Leave `defaultdb` intact (or revert Railway `DB_URL` back to `defaultdb` and redeploy prior commit `3744422`), then inspect/drop `gscores_v2`.
* **Option 2 Rollback**:
  * **Never rely on `pg_restore --clean` alone** — `--clean` leaves post-backup objects such as `import_metadata` behind while reverting `flyway_schema_history` to `V1`–`V4`.
  * **Preferred**: Create a new clean DB (`CREATE DATABASE gscores_rollback;`), restore `aiven_pre_v6_backup.dump` with PG 18 `pg_restore`, verify all 5 criteria, and point Railway `DB_URL` to `gscores_rollback` + redeploy commit `3744422`.
  * **If forced to restore onto `defaultdb` directly**: Stop all Railway connections, **rehearse the full cleanup + restore cycle on `gscores-pg18-verify` first**, drop all non-extension (`deptype <> 'e'`) materialized views (`relkind = 'm'`), views (`'v'`), tables (`'r','p'`), sequences (`'S'`), and routines (`pg_proc` `'f','p','w'`) in schema `public`, verify all 5 extension-filtered remaining-object counts equal `0`, then run PG 18 `pg_restore --exit-on-error` from `aiven_pre_v6_backup.dump` and confirm `to_regclass('public.import_metadata') IS NULL`.

---

## 🧪 Testing & Verification

```bash
# Backend Unit & Concurrency Tests
cd backend && .\mvnw.cmd test   # Windows
cd backend && ./mvnw test       # Linux / macOS

# Frontend Lint & Production Build
cd frontend && npm run lint && npm run build

# Docker Compose Validation
docker compose config
```
