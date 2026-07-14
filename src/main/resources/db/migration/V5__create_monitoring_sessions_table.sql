CREATE TABLE IF NOT EXISTS monitoring_sessions (
    id BIGSERIAL PRIMARY KEY,
    monitor_registration VARCHAR(255) NOT NULL,
    monitoring_id BIGINT NOT NULL,
    monitoring_schedule_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    description TEXT,
    is_started BOOLEAN NOT NULL,

    CONSTRAINT monitoring_sessions_monitor_registration_fk FOREIGN KEY (monitor_registration) REFERENCES users(registration) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT monitoring_sessions_monitoring_id_fk FOREIGN KEY (monitoring_id) REFERENCES monitoring(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT monitoring_sessions_monitoring_schedule_id_fk FOREIGN KEY (monitoring_schedule_id) REFERENCES monitoring_schedules(id) ON DELETE RESTRICT ON UPDATE CASCADE
);
