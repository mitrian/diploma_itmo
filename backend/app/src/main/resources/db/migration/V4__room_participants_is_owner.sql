ALTER TABLE room_participants
	ADD COLUMN is_owner BOOLEAN NOT NULL DEFAULT false;

UPDATE room_participants rp
SET is_owner = true
FROM rooms r
WHERE rp.room_id = r.id
	AND rp.user_id = r.owner_id;
