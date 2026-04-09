CREATE TABLE IF NOT EXISTS appointment_service_bootstrap (
    service_name VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO appointment_service_bootstrap (service_name)
VALUES ('appointment-service')
ON CONFLICT (service_name) DO NOTHING;

