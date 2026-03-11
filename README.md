# FlowHR

**FlowHR** adalah sistem HRIS (Human Resource Information System) berbasis web untuk perusahaan skala menengah ke atas. Sistem ini mengotomatisasi seluruh siklus hidup karyawan dari rekrutmen hingga pensiun (Recruit-to-Retire).

## Modules

- **core-hr** — Employee Master Data
- **ats** — Recruitment & Applicant Tracking System
- **onboarding** — Onboarding & Offboarding
- **attendance** — Kehadiran & Cuti
- **claims** — Claims & Expense Reimbursement
- **payroll** — Payroll & Compensation
- **performance** — Performance Management
- **analytics** — Analytics & Reporting

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- Docker & Docker Compose

### 1. Clone & Setup Environment

```bash
git clone https://github.com/akbarwjyy/flowhr.git
cd flowhr
cp .env.example .env
# Edit .env with your credentials
```

### 2. Run with Docker Compose (Recommended)

```bash
docker-compose up -d
```

Services:
- PostgreSQL → `localhost:5432`
- Backend (Spring Boot) → `http://localhost:8080`
- Frontend (Astro) → `http://localhost:4321`

### 3. Run Manually

**Backend:**
```bash
cd server
.\mvnw.cmd spring-boot:run
```

**Frontend:**
```bash
cd client
npm install
npm run dev
```

## API

Base URL: `http://localhost:8080/api/v1`

### Authentication
```
POST /api/v1/auth/login
Body: { "username": "...", "password": "..." }
Response: { "token": "JWT_TOKEN" }
```

All protected endpoints require header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Default Roles

| Role | Description |
|------|-------------|
| `ROLE_SUPER_ADMIN` | Full system access |
| `ROLE_HR_ADMIN` | HR operations |
| `ROLE_MANAGER` | Team management |
| `ROLE_EMPLOYEE` | Self-service |
