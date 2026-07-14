CREATE TABLE IF NOT EXISTS monitoring_schedules (
    id BIGSERIAL PRIMARY KEY,
    monitor_registration VARCHAR(255) NOT NULL,
    monitoring_id BIGINT NOT NULL,
    day_of_week VARCHAR(20),
    start_time TIME,
    end_time TIME,
    status VARCHAR(20),
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT monitoring_schedules_monitor_registration_fk FOREIGN KEY (monitor_registration) REFERENCES users (registration) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT monitoring_schedules_monitoring_id_fk FOREIGN KEY (monitoring_id) REFERENCES monitoring (id) ON DELETE RESTRICT ON UPDATE CASCADE
);
