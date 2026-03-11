-- V9: Onboarding & Offboarding Tasks

CREATE TYPE onboarding_event AS ENUM ('ONBOARDING', 'OFFBOARDING');
CREATE TYPE task_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED');

-- Template tasks (reusable per event type)
CREATE TABLE onboarding_task_templates (
    id              BIGSERIAL PRIMARY KEY,
    event_type      onboarding_event NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    responsible_dept VARCHAR(100) NOT NULL,  -- IT, GA, HR, Finance
    order_index     INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Instantiated tasks per employee per event
CREATE TABLE onboarding_processes (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    event_type      onboarding_event NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE onboarding_task_assignments (
    id              BIGSERIAL PRIMARY KEY,
    process_id      BIGINT NOT NULL REFERENCES onboarding_processes(id) ON DELETE CASCADE,
    template_id     BIGINT REFERENCES onboarding_task_templates(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    responsible_dept VARCHAR(100) NOT NULL,
    assigned_to     BIGINT REFERENCES users(id),
    due_date        DATE,
    status          task_status NOT NULL DEFAULT 'PENDING',
    completed_at    TIMESTAMPTZ,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed default onboarding templates
INSERT INTO onboarding_task_templates (event_type, title, description, responsible_dept, order_index) VALUES
    ('ONBOARDING', 'Persiapan Laptop & Akun Email',     'Setup laptop, akun email, dan akun sistem',      'IT',      1),
    ('ONBOARDING', 'Orientasi Kebijakan Perusahaan',    'Pengenalan kebijakan, SOP, dan budaya perusahaan','HR',      2),
    ('ONBOARDING', 'Penanda tangan Kontrak Kerja',      'Tanda tangan kontrak dan dokumen HR',             'HR',      3),
    ('ONBOARDING', 'Setup Kartu Akses & ID Card',       'Pembuatan ID card dan kartu akses gedung',        'GA',      4),
    ('ONBOARDING', 'Setup Rekening Gaji',               'Pendaftaran rekening bank untuk pencairan gaji',  'Finance', 5),
    ('ONBOARDING', 'Pengenalan Tim & Workspace',        'Perkenalan dengan tim dan setup area kerja',      'GA',      6),
    ('OFFBOARDING','Exit Interview',                    'Wawancara perpisahan dengan HR',                  'HR',      1),
    ('OFFBOARDING','Pengembalian Aset Perusahaan',     'Pengembalian laptop, kartu akses, ID card',       'GA',      2),
    ('OFFBOARDING','Penonaktifan Akun Sistem',          'Nonaktifkan email, akses aplikasi, VPN',          'IT',      3),
    ('OFFBOARDING','Penyelesaian Payroll Terakhir',    'Kalkulasi gaji, pesangon, dan uang cuti tersisa',  'Finance', 4);

CREATE INDEX idx_onboarding_assignments_process ON onboarding_task_assignments(process_id);
CREATE INDEX idx_onboarding_assignments_status ON onboarding_task_assignments(status);
