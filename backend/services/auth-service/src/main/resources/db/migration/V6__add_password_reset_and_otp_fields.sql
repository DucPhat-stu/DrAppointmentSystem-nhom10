-- V6: Add password reset, OTP, 2FA, and doctor code fields to users table
-- These are additive changes — no existing columns or constraints are modified.

ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expires_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_otp VARCHAR(6);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_otp_expires_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(64);
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS doctor_code VARCHAR(32);

-- Index for password reset token lookup
CREATE INDEX IF NOT EXISTS idx_users_password_reset_token ON users(password_reset_token)
    WHERE password_reset_token IS NOT NULL;

-- Index for doctor code login
CREATE INDEX IF NOT EXISTS idx_users_doctor_code ON users(doctor_code)
    WHERE doctor_code IS NOT NULL;
