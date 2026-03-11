-- V8: Performance Management (Goals & Appraisals)

CREATE TYPE goal_status AS ENUM ('ACTIVE', 'COMPLETED', 'CANCELLED');
CREATE TYPE appraisal_status AS ENUM ('DRAFT', 'SUBMITTED', 'REVIEWED', 'FINAL');
CREATE TYPE appraisal_period AS ENUM ('QUARTERLY', 'ANNUAL');

-- Goals / KPI / OKR
CREATE TABLE goals (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    target          TEXT NOT NULL,         -- apa yang ingin dicapai
    metric          VARCHAR(100),          -- satuan ukur
    target_value    DECIMAL(10, 2),        -- nilai target
    actual_value    DECIMAL(10, 2),        -- nilai aktual
    weight          INT NOT NULL DEFAULT 100, -- bobot dalam persen (total 100%)
    status          goal_status NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    period_year     INT NOT NULL,
    period_month    INT,                   -- null = annual goal
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Appraisal cycles
CREATE TABLE appraisals (
    id                  BIGSERIAL PRIMARY KEY,
    employee_id         BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    reviewer_id         BIGINT NOT NULL REFERENCES employees(id),
    period              appraisal_period NOT NULL,
    period_year         INT NOT NULL,
    period_quarter      INT,               -- 1-4, null jika annual
    overall_score       DECIMAL(4, 2),     -- 0.00 - 5.00
    goal_achievement    TEXT,              -- narasi pencapaian
    strengths           TEXT,
    improvements        TEXT,
    development_plan    TEXT,
    bonus_recommendation NUMERIC(15, 2),
    promotion_recommendation BOOLEAN DEFAULT FALSE,
    status              appraisal_status NOT NULL DEFAULT 'DRAFT',
    submitted_at        TIMESTAMPTZ,
    reviewed_at         TIMESTAMPTZ,
    finalized_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(employee_id, period, period_year, period_quarter)
);

-- Appraisal detail per goal
CREATE TABLE appraisal_goal_scores (
    id              BIGSERIAL PRIMARY KEY,
    appraisal_id    BIGINT NOT NULL REFERENCES appraisals(id) ON DELETE CASCADE,
    goal_id         BIGINT NOT NULL REFERENCES goals(id),
    score           DECIMAL(4, 2) NOT NULL,  -- 0.00 - 5.00
    notes           TEXT
);

CREATE INDEX idx_goals_employee ON goals(employee_id);
CREATE INDEX idx_goals_period ON goals(period_year);
CREATE INDEX idx_appraisals_employee ON appraisals(employee_id);
CREATE INDEX idx_appraisals_period ON appraisals(period_year);
