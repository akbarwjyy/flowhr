-- V1: Users, Roles, User_Roles
-- FlowHR Authentication & Authorization Tables

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,  -- e.g. ROLE_HR_ADMIN
    description VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,  -- BCrypt hashed
    email        VARCHAR(150) NOT NULL UNIQUE,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seed default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_SUPER_ADMIN', 'Akses penuh ke seluruh sistem'),
    ('ROLE_HR_ADMIN',    'Operasional HR: rekrutmen, payroll, laporan'),
    ('ROLE_MANAGER',     'Approve cuti, klaim, dan penilaian kinerja tim'),
    ('ROLE_EMPLOYEE',    'Self-service: presensi, cuti, klaim, slip gaji');

-- Seed default super admin user (password: Admin@FlowHR2024)
INSERT INTO users (username, password, email) VALUES (
    'superadmin',
    '$2a$12$bKitfkZYMPD7aEWjF5FBzuPQnF1fX9Y7Cv0RxHg.8wPXXu4EfJdJW',
    'superadmin@flowhr.id'
);

-- Assign ROLE_SUPER_ADMIN to superadmin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'superadmin' AND r.name = 'ROLE_SUPER_ADMIN';
