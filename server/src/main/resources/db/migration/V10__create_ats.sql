-- V10: ATS — Applicant Tracking System

CREATE TYPE job_status AS ENUM ('DRAFT', 'OPEN', 'CLOSED', 'FILLED');
CREATE TYPE employment_type AS ENUM ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE');
CREATE TYPE applicant_stage AS ENUM ('APPLIED', 'SCREENING', 'HR_INTERVIEW', 'USER_INTERVIEW', 'OFFERING', 'HIRED', 'REJECTED', 'WITHDRAWN');

-- Job postings
CREATE TABLE jobs (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    department_id       BIGINT REFERENCES departments(id),
    employment_type     employment_type NOT NULL DEFAULT 'FULL_TIME',
    location            VARCHAR(150),
    description         TEXT NOT NULL,
    requirements        TEXT,
    salary_min          NUMERIC(15, 2),
    salary_max          NUMERIC(15, 2),
    is_salary_visible   BOOLEAN NOT NULL DEFAULT FALSE,
    status              job_status NOT NULL DEFAULT 'DRAFT',
    published_at        TIMESTAMPTZ,
    closed_at           TIMESTAMPTZ,
    created_by          BIGINT REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Applicant profiles
CREATE TABLE applicants (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100),
    email           VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    cv_url          VARCHAR(500),          -- uploaded CV
    portfolio_url   VARCHAR(500),
    linkedin_url    VARCHAR(500),
    source          VARCHAR(100),          -- Website, LinkedIn, Referral, etc
    current_company VARCHAR(150),
    current_position VARCHAR(150),
    expected_salary NUMERIC(15, 2),
    stage           applicant_stage NOT NULL DEFAULT 'APPLIED',
    notes           TEXT,                  -- catatan rekrutmen
    applied_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Interviews
CREATE TABLE interviews (
    id              BIGSERIAL PRIMARY KEY,
    applicant_id    BIGINT NOT NULL REFERENCES applicants(id) ON DELETE CASCADE,
    interview_type  VARCHAR(50) NOT NULL,  -- HR_INTERVIEW, USER_INTERVIEW, TECHNICAL
    scheduled_at    TIMESTAMPTZ NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 60,
    location        VARCHAR(200),           -- Ruang rapat / Google Meet link
    interviewer_id  BIGINT REFERENCES employees(id),
    rating          INT CHECK (rating BETWEEN 1 AND 5),
    feedback        TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED, DONE, CANCELLED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Stage history (audit trail pipeline)
CREATE TABLE applicant_stage_history (
    id              BIGSERIAL PRIMARY KEY,
    applicant_id    BIGINT NOT NULL REFERENCES applicants(id) ON DELETE CASCADE,
    from_stage      applicant_stage,
    to_stage        applicant_stage NOT NULL,
    changed_by      BIGINT REFERENCES users(id),
    notes           TEXT,
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_applicants_job ON applicants(job_id);
CREATE INDEX idx_applicants_stage ON applicants(stage);
CREATE INDEX idx_interviews_applicant ON interviews(applicant_id);
CREATE INDEX idx_interviews_scheduled ON interviews(scheduled_at);
