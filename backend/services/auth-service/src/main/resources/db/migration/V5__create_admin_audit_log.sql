-- V5: Admin Audit Log table
CREATE TABLE admin_audit_log (
    id          UUID        NOT NULL PRIMARY KEY,
    admin_id    UUID        NOT NULL,
    action      VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id   UUID        NOT NULL,
    detail      TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_admin_audit_log_admin_id    ON admin_audit_log (admin_id);
CREATE INDEX idx_admin_audit_log_target_id   ON admin_audit_log (target_id);
CREATE INDEX idx_admin_audit_log_created_at  ON admin_audit_log (created_at DESC);
