-- V7: Reset admin password to Admin@123 (freshly generated bcrypt cost 12)
UPDATE users
SET password_hash = '$2a$12$RCxGB40ALN5Mo3p14sEeAO/OJEU8JZyvFe5wzYXR2A6YLevW1P7aa',
    failed_login_attempts = 0,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin01@healthcare.local';
