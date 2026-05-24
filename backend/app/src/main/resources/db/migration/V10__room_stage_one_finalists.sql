CREATE TABLE room_stage_one_finalists (
	id BIGSERIAL PRIMARY KEY,
	room_id BIGINT NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
	restaurant_id BIGINT NOT NULL REFERENCES restaurants (id) ON DELETE RESTRICT,
	approval_count INT NOT NULL,
	included_by VARCHAR(31) NOT NULL,
	position INT NOT NULL,
	CONSTRAINT uq_room_stage_one_finalist_restaurant UNIQUE (room_id, restaurant_id),
	CONSTRAINT uq_room_stage_one_finalist_position UNIQUE (room_id, position)
);

CREATE INDEX idx_room_stage_one_finalists_room_pos ON room_stage_one_finalists (room_id, position);
