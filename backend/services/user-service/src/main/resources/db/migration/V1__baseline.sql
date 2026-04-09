CREATE TABLE IF NOT EXISTS user_service_bootstrap (
    service_name VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user_service_bootstrap (service_name)
VALUES ('user-service')
ON CONFLICT (service_name) DO NOTHING;

