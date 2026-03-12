-- V3: Employees table — core HR master data
-- Includes JSONB custom_fields as per context.md spec

CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- for full-text search

CREATE TYPE employment_status AS ENUM ('ACTIVE', 'PROBATION', 'RESIGNED', 'TERMINATED', 'RETIRED');
CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE');
CREATE TYPE marital_status AS ENUM ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED');

CREATE TABLE employees (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT UNIQUE REFERENCES users(id),  -- linked system user account

    -- Identitas
    nip              VARCHAR(50) NOT NULL UNIQUE,           -- Nomor Induk Pegawai
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100),
    gender           gender_type,
    birth_date       DATE,
    birth_place      VARCHAR(100),
    marital_status   marital_status DEFAULT 'SINGLE',
    religion         VARCHAR(50),
    nationality      VARCHAR(50) DEFAULT 'Indonesia',
    photo_url        VARCHAR(500),

    -- Kontak
    phone            VARCHAR(50),
    email_personal   VARCHAR(150),
    address          TEXT,
    city             VARCHAR(100),
    province         VARCHAR(100),
    postal_code      VARCHAR(20),

    -- Identitas Legal
    nik              VARCHAR(20) UNIQUE,   -- KTP
    npwp             VARCHAR(50) UNIQUE,   -- NPWP
    bpjs_kes         VARCHAR(30),          -- BPJS Kesehatan
    bpjs_tk          VARCHAR(30),          -- BPJS Ketenagakerjaan

    -- Pekerjaan
    department_id    BIGINT REFERENCES departments(id),
    position_id      BIGINT REFERENCES positions(id),
    employment_status employment_status NOT NULL DEFAULT 'PROBATION',
    join_date        DATE NOT NULL,
    end_date         DATE,
    direct_manager_id BIGINT REFERENCES employees(id),

    -- Gaji
    base_salary      NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Custom Fields (JSONB — misal: ukuran_seragam, alergi, dsb)
    custom_fields    JSONB DEFAULT '{}'::jsonb,

    -- Audit
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index untuk pencarian
CREATE INDEX idx_employees_name ON employees USING gin(
    to_tsvector('indonesian', first_name || ' ' || COALESCE(last_name, ''))
);
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(employment_status);
CREATE INDEX idx_employees_custom_fields ON employees USING gin(custom_fields);

-- Emergency contacts
CREATE TABLE emergency_contacts (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name          VARCHAR(150) NOT NULL,
    relationship  VARCHAR(50) NOT NULL,
    phone         VARCHAR(20) NOT NULL,
    address       TEXT
);

-- Bank accounts
CREATE TABLE employee_bank_accounts (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    bank_name     VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder_name VARCHAR(150) NOT NULL,
    is_primary    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Job history (mutasi, promosi, rotasi)
CREATE TABLE employee_job_history (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    department_id BIGINT REFERENCES departments(id),
    position_id   BIGINT REFERENCES positions(id),
    base_salary   NUMERIC(15, 2),
    effective_date DATE NOT NULL,
    reason        VARCHAR(200),  -- Promosi, Mutasi, Penyesuaian Gaji, dll
    notes         TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Employee documents
CREATE TABLE employee_documents (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    doc_type      VARCHAR(50) NOT NULL,  -- KTP, NPWP, IJAZAH, KONTRAK, dll
    file_url      VARCHAR(500) NOT NULL,
    file_name     VARCHAR(255),
    uploaded_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by   BIGINT REFERENCES users(id)
);

-- Asset tracking
CREATE TABLE employee_assets (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL REFERENCES employees(id),
    asset_name    VARCHAR(150) NOT NULL,  -- Laptop, Monitor, Kartu Akses, dll
    asset_code    VARCHAR(50),
    serial_number VARCHAR(100),
    assigned_date DATE NOT NULL,
    returned_date DATE,
    condition     VARCHAR(50),  -- GOOD, DAMAGED, LOST
    notes         TEXT
);
