ALTER TABLE rooms
	ADD COLUMN stage_one_vote_rows_count INTEGER NOT NULL DEFAULT 0;

UPDATE rooms r
SET stage_one_vote_rows_count = src.vote_rows_count
FROM (
	SELECT room_id, COUNT(*)::INTEGER AS vote_rows_count
	FROM room_stage_one_votes
	GROUP BY room_id
) src
WHERE r.id = src.room_id;

ALTER TABLE room_stage_one_candidates
	ADD COLUMN base_reached_at TIMESTAMP,
	ADD COLUMN relaxed_reached_at TIMESTAMP,
	ADD COLUMN base_weighted_reached_at TIMESTAMP,
	ADD COLUMN relaxed_weighted_reached_at TIMESTAMP;
