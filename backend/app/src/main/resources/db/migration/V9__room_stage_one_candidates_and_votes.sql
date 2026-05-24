CREATE TABLE room_stage_one_candidates (
	id BIGSERIAL PRIMARY KEY,
	room_id BIGINT NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
	sort_order INT NOT NULL,
	restaurant_id BIGINT NOT NULL REFERENCES restaurants (id) ON DELETE RESTRICT,
	CONSTRAINT uq_room_stage_one_sort UNIQUE (room_id, sort_order),
	CONSTRAINT uq_room_stage_one_restaurant UNIQUE (room_id, restaurant_id)
);

CREATE INDEX idx_rsoc_room_sort ON room_stage_one_candidates (room_id, sort_order);

CREATE TABLE room_stage_one_votes (
	id BIGSERIAL PRIMARY KEY,
	room_id BIGINT NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
	restaurant_id BIGINT NOT NULL REFERENCES restaurants (id) ON DELETE RESTRICT,
	suitable BOOLEAN NOT NULL,
	created_at TIMESTAMP NOT NULL,
	CONSTRAINT uq_room_user_restaurant_stage_one UNIQUE (room_id, user_id, restaurant_id)
);
