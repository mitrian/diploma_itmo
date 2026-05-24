ALTER TABLE rooms
	ADD COLUMN participant_count INTEGER NOT NULL DEFAULT 0,
	ADD COLUMN stage_one_participant_count_snapshot INTEGER;
