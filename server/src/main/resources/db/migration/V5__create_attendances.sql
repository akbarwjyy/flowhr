-- V5: Attendance (Clock-in/Clock-out & Attendance Logs)

CREATE TYPE attendance_status AS ENUM ('PRESENT', 'LATE', 'ABSENCE', 'HALF_DAY', 'ON_LEAVE', 'HOLIDAY');

CREATE TABLE attendances (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    clock_in        TIMESTAMPTZ,
    clock_out       TIMESTAMPTZ,
    clock_in_lat    DECIMAL(10, 8),   -- geolocation (dummy)
    clock_in_lng    DECIMAL(11, 8),
    clock_out_lat   DECIMAL(10, 8),
    clock_out_lng   DECIMAL(11, 8),
    status          attendance_status NOT NULL DEFAULT 'PRESENT',
    work_minutes    INT,              -- dihitung saat clock-out
    overtime_minutes INT DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, attendance_date)
);

-- Work schedules
CREATE TABLE work_schedules (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,    -- e.g. WFO Shift A, WFH
    clock_in_time   TIME NOT NULL DEFAULT '08:00:00',
    clock_out_time  TIME NOT NULL DEFAULT '17:00:00',
    late_threshold_minutes INT NOT NULL DEFAULT 15,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO work_schedules (name, clock_in_time, clock_out_time, late_threshold_minutes, is_default) VALUES
    ('WFO Standard', '08:00:00', '17:00:00', 15, TRUE),
    ('WFH Standard', '09:00:00', '18:00:00', 15, FALSE);

CREATE INDEX idx_attendances_employee_date ON attendances(employee_id, attendance_date);
CREATE INDEX idx_attendances_date ON attendances(attendance_date);
