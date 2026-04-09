CREATE TABLE IF NOT EXISTS doctor_service_bootstrap (
    service_name VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO doctor_service_bootstrap (service_name)
VALUES ('doctor-service')
ON CONFLICT (service_name) DO NOTHING;

