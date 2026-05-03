ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reminder_24h_sent_at TIMESTAMPTZ;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS reminder_1h_sent_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_appointments_reminder_24h
    ON appointments(status, scheduled_start)
    WHERE reminder_24h_sent_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_appointments_reminder_1h
    ON appointments(status, scheduled_start)
    WHERE reminder_1h_sent_at IS NULL;
