ALTER TABLE room_stage_one_candidates
	ADD COLUMN suitable_count INTEGER NOT NULL DEFAULT 0,
	ADD COLUMN suitable_weighted_count INTEGER NOT NULL DEFAULT 0;

UPDATE room_stage_one_candidates c
SET suitable_count = src.suitable_count
FROM (
	SELECT
		v.room_id,
		v.restaurant_id,
		COUNT(*)::INTEGER AS suitable_count
	FROM room_stage_one_votes v
	WHERE v.suitable = TRUE
	GROUP BY v.room_id, v.restaurant_id
) src
WHERE c.room_id = src.room_id
	AND c.restaurant_id = src.restaurant_id;

UPDATE room_stage_one_candidates c
SET suitable_weighted_count = src.suitable_weighted_count
FROM (
	SELECT
		v.room_id,
		v.restaurant_id,
		SUM(CASE WHEN v.user_id = r.owner_id THEN 2 ELSE 1 END)::INTEGER AS suitable_weighted_count
	FROM room_stage_one_votes v
	JOIN rooms r ON r.id = v.room_id
	WHERE v.suitable = TRUE
	GROUP BY v.room_id, v.restaurant_id
) src
WHERE c.room_id = src.room_id
	AND c.restaurant_id = src.restaurant_id;
