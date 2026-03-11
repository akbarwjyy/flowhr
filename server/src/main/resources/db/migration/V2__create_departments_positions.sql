-- V2: Departments & Positions (prerequisite for employees)

CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300),
    manager_id  BIGINT,  -- FK to employees (added later via ALTER)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE positions (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(150) NOT NULL,
    department_id BIGINT NOT NULL REFERENCES departments(id),
    level         VARCHAR(50),  -- Junior, Mid, Senior, Lead, Manager
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed departments
INSERT INTO departments (name, description) VALUES
    ('Human Resources',       'Departemen sumber daya manusia'),
    ('Information Technology','Departemen teknologi informasi'),
    ('Finance',               'Departemen keuangan dan akuntansi'),
    ('Operations',            'Departemen operasional umum'),
    ('Marketing',             'Departemen pemasaran dan komunikasi'),
    ('General Affairs',       'Departemen umum dan fasilitas');

-- Seed positions
INSERT INTO positions (title, department_id, level) VALUES
    ('HR Manager',        (SELECT id FROM departments WHERE name='Human Resources'), 'Manager'),
    ('HR Staff',          (SELECT id FROM departments WHERE name='Human Resources'), 'Junior'),
    ('IT Manager',        (SELECT id FROM departments WHERE name='Information Technology'), 'Manager'),
    ('Backend Engineer',  (SELECT id FROM departments WHERE name='Information Technology'), 'Mid'),
    ('Frontend Engineer', (SELECT id FROM departments WHERE name='Information Technology'), 'Mid'),
    ('Finance Manager',   (SELECT id FROM departments WHERE name='Finance'), 'Manager'),
    ('Accountant',        (SELECT id FROM departments WHERE name='Finance'), 'Mid'),
    ('Operations Manager',(SELECT id FROM departments WHERE name='Operations'), 'Manager'),
    ('GA Staff',          (SELECT id FROM departments WHERE name='General Affairs'), 'Junior');
