ALTER TABLE rooms
    ADD COLUMN stage_one_timeout_at TIMESTAMP NULL,
    ADD COLUMN stage_two_timeout_at TIMESTAMP NULL,
    ADD COLUMN stage_one_timeout_processed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN stage_two_timeout_processed BOOLEAN NOT NULL DEFAULT FALSE;
