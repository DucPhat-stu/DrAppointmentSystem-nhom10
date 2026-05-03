-- V4: Seed admin account
-- Password: Admin@123 (bcrypt cost 12)
INSERT INTO users (
    id,
    email,
    password_hash,
    role,
    status,
    full_name,
    phone,
    failed_login_attempts,
    created_at,
    updated_at
)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    'admin01@healthcare.local',
    '$2a$12$ByNnmAEZuGCrVWJIAcR/7.IYDIX1C1ZbhVUSAqiywkTwD62Jd2w0W',
    'ADMIN',
    'ACTIVE',
    'Admin Seed',
    '0901000003',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
