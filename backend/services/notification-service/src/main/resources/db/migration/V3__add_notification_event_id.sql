ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS event_id UUID;

DROP INDEX IF EXISTS uq_notifications_event_recipient;

CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_event_id_recipient
    ON notifications (event_id, recipient_id)
    WHERE event_id IS NOT NULL;
