CREATE TABLE room_stage_two_ranks (
	id BIGSERIAL PRIMARY KEY,
	room_id BIGINT NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
	restaurant_id BIGINT NOT NULL REFERENCES restaurants (id) ON DELETE RESTRICT,
	rank_value INT NOT NULL,
	CONSTRAINT uq_room_stage_two_rank_user_restaurant UNIQUE (room_id, user_id, restaurant_id)
);

CREATE INDEX idx_room_stage_two_ranks_room ON room_stage_two_ranks (room_id);
CREATE INDEX idx_room_stage_two_ranks_room_user ON room_stage_two_ranks (room_id, user_id);

ALTER TABLE rooms ADD COLUMN chosen_restaurant_id BIGINT NULL REFERENCES restaurants (id) ON DELETE RESTRICT;
