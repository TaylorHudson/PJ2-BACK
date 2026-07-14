CREATE TABLE IF NOT EXISTS monitoring (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    allow_monitors_same_time BOOLEAN NOT NULL,
    teacher_registration VARCHAR(255) NOT NULL,
    topics TEXT,
    CONSTRAINT monitoring_teacher_registration_fk FOREIGN KEY (teacher_registration) REFERENCES users(registration) ON DELETE RESTRICT ON UPDATE CASCADE
);
