-- V7: Payroll Batches & Payslips

CREATE TYPE payroll_batch_status AS ENUM ('DRAFT', 'PROCESSING', 'DONE', 'CANCELLED');
CREATE TYPE payslip_status AS ENUM ('DRAFT', 'GENERATED', 'SENT');

CREATE TABLE payroll_batches (
    id              BIGSERIAL PRIMARY KEY,
    period_year     INT NOT NULL,
    period_month    INT NOT NULL,   -- 1-12
    department_id   BIGINT REFERENCES departments(id),  -- NULL = all departments
    status          payroll_batch_status NOT NULL DEFAULT 'DRAFT',
    processed_by    BIGINT REFERENCES users(id),
    processed_at    TIMESTAMPTZ,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(period_year, period_month, department_id)
);

CREATE TABLE payslips (
    id                 BIGSERIAL PRIMARY KEY,
    batch_id           BIGINT NOT NULL REFERENCES payroll_batches(id) ON DELETE CASCADE,
    employee_id        BIGINT NOT NULL REFERENCES employees(id),

    -- Komponen penghasilan
    base_salary        NUMERIC(15, 2) NOT NULL DEFAULT 0,
    meal_allowance     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    transport_allowance NUMERIC(15, 2) NOT NULL DEFAULT 0,
    other_allowance    NUMERIC(15, 2) NOT NULL DEFAULT 0,
    reimbursement_total NUMERIC(15, 2) NOT NULL DEFAULT 0,  -- dari claims

    -- Komponen potongan
    late_deduction     NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- keterlambatan
    pph21              NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- pajak penghasilan
    bpjs_kes_employee  NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- 1% gaji
    bpjs_tk_employee   NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- 2% gaji
    other_deduction    NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Kalkulasi
    gross_salary       NUMERIC(15, 2) NOT NULL,
    total_deduction    NUMERIC(15, 2) NOT NULL,
    net_salary         NUMERIC(15, 2) NOT NULL,

    -- PDF
    pdf_url            VARCHAR(500),   -- password-protected PDF
    pdf_password       VARCHAR(100),   -- e.g. FLOWHR_NIP_1234

    status             payslip_status NOT NULL DEFAULT 'DRAFT',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(batch_id, employee_id)
);

-- Add FK back to claims
ALTER TABLE claims ADD CONSTRAINT fk_claims_payroll_batch
    FOREIGN KEY (payroll_batch_id) REFERENCES payroll_batches(id);

CREATE INDEX idx_payslips_employee ON payslips(employee_id);
CREATE INDEX idx_payslips_batch ON payslips(batch_id);
CREATE INDEX idx_payroll_batches_period ON payroll_batches(period_year, period_month);
