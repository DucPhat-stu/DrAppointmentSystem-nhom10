CREATE TABLE IF NOT EXISTS notification_service_bootstrap (
    service_name VARCHAR(64) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO notification_service_bootstrap (service_name)
VALUES ('notification-service')
ON CONFLICT (service_name) DO NOTHING;
