-- V4: Leave Quotas & Leave Requests

CREATE TYPE leave_type AS ENUM ('ANNUAL', 'SICK', 'MATERNITY', 'PATERNITY', 'MARRIAGE', 'BEREAVEMENT', 'UNPAID');
CREATE TYPE leave_status AS ENUM ('PENDING', 'APPROVED_MANAGER', 'APPROVED_HR', 'REJECTED', 'CANCELLED');

-- Kuota cuti per karyawan per tahun
CREATE TABLE leave_quotas (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type      leave_type NOT NULL,
    year            INT NOT NULL,
    total_days      INT NOT NULL DEFAULT 12,
    used_days       INT NOT NULL DEFAULT 0,
    remaining_days  INT NOT NULL DEFAULT 12,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, leave_type, year)
);

-- Pengajuan cuti
CREATE TABLE leaves (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type      leave_type NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    total_days      INT NOT NULL,
    reason          TEXT NOT NULL,
    status          leave_status NOT NULL DEFAULT 'PENDING',
    attachment_url  VARCHAR(500),

    -- Multi-level approval
    manager_id      BIGINT REFERENCES employees(id),
    manager_approved_at TIMESTAMPTZ,
    manager_notes   TEXT,

    hr_id           BIGINT REFERENCES employees(id),
    hr_approved_at  TIMESTAMPTZ,
    hr_notes        TEXT,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leaves_employee ON leaves(employee_id);
CREATE INDEX idx_leaves_status ON leaves(status);
CREATE INDEX idx_leaves_dates ON leaves(start_date, end_date);
CREATE INDEX idx_leave_quotas_employee_year ON leave_quotas(employee_id, year);
