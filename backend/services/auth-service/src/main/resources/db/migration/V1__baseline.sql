CREATE TABLE IF NOT EXISTS auth_service_bootstrap (
    service_name VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO auth_service_bootstrap (service_name)
VALUES ('auth-service')
ON CONFLICT (service_name) DO NOTHING;

