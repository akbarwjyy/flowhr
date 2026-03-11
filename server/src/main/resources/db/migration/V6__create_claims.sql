-- V6: Claims & Expense Reimbursement

CREATE TYPE claim_status AS ENUM ('DRAFT', 'SUBMITTED', 'APPROVED_MANAGER', 'APPROVED_HR', 'REJECTED', 'PROCESSED');

CREATE TABLE claim_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,   -- Medis, Transportasi, Business Trip
    description VARCHAR(300),
    max_amount  NUMERIC(15, 2),                -- null = unlimited
    requires_receipt BOOLEAN NOT NULL DEFAULT TRUE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE claims (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES claim_categories(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    amount          NUMERIC(15, 2) NOT NULL,
    claim_date      DATE NOT NULL,
    receipt_url     VARCHAR(500),   -- upload struk/kuitansi
    status          claim_status NOT NULL DEFAULT 'DRAFT',

    -- Approval
    manager_id      BIGINT REFERENCES employees(id),
    manager_approved_at TIMESTAMPTZ,
    manager_notes   TEXT,

    hr_id           BIGINT REFERENCES employees(id),
    hr_approved_at  TIMESTAMPTZ,
    hr_notes        TEXT,

    -- Payroll integration: dimasukkan ke payroll bulan berjalan
    payroll_batch_id BIGINT,   -- FK added in V7

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed claim categories
INSERT INTO claim_categories (name, description, max_amount, requires_receipt) VALUES
    ('Medis',          'Biaya pengobatan dan kesehatan', 2000000.00,  TRUE),
    ('Transportasi',   'Biaya transportasi dinas',       500000.00,   TRUE),
    ('Business Trip',  'Biaya perjalanan dinas',         NULL,        TRUE),
    ('Komunikasi',     'Pulsa dan internet dinas',       300000.00,   FALSE),
    ('Makan Dinas',    'Biaya makan saat perjalanan',    150000.00,   TRUE);

CREATE INDEX idx_claims_employee ON claims(employee_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_category ON claims(category_id);
