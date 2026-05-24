ALTER TABLE restaurants
	ADD COLUMN kudago_place_id BIGINT NULL;

ALTER TABLE restaurants
	ADD CONSTRAINT uq_restaurants_kudago_place_id UNIQUE (kudago_place_id);
