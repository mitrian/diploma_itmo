ALTER TABLE room_participants
	ADD COLUMN filters_confirmed BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE kitchen_tags (
	id BIGSERIAL PRIMARY KEY,
	slug VARCHAR(63) NOT NULL UNIQUE,
	label_ru VARCHAR(511) NOT NULL
);

INSERT INTO kitchen_tags (slug, label_ru) VALUES
	('european', 'европейская'),
	('russian', 'русская'),
	('american', 'американская'),
	('italian', 'итальянская'),
	('japanese', 'японская'),
	('pan-asian-cuisine', 'паназиатская'),
	('chinese', 'китайская'),
	('thai', 'тайская'),
	('korean', 'корейская'),
	('georgian', 'грузинская'),
	('armenian', 'армянская'),
	('caucasus', 'кавказская'),
	('indian', 'индийская'),
	('mexican', 'мексиканская'),
	('middle-eastern', 'ближневосточная / восточная'),
	('german', 'немецкая'),
	('spanish', 'испанская'),
	('street-food', 'стритфуд'),
	('fast-food', 'фастфуд'),
	('vegetarian', 'вегетарианская'),
	('ethnic-cuisine', 'этническая кухня (общий тег)'),
	('gastronomic', 'гастрономическая');

CREATE TABLE restaurants (
	id BIGSERIAL PRIMARY KEY
);

CREATE TABLE restaurant_kitchen_tags (
	id BIGSERIAL PRIMARY KEY,
	restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
	kitchen_tag_id BIGINT NOT NULL REFERENCES kitchen_tags(id) ON DELETE CASCADE,
	CONSTRAINT uq_restaurant_kitchen_tag UNIQUE (restaurant_id, kitchen_tag_id)
);

CREATE INDEX idx_rkt_restaurant ON restaurant_kitchen_tags (restaurant_id);
CREATE INDEX idx_rkt_kitchen_tag ON restaurant_kitchen_tags (kitchen_tag_id);

CREATE TABLE room_kitchen_tag_selections (
	id BIGSERIAL PRIMARY KEY,
	room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	kitchen_tag_id BIGINT NOT NULL REFERENCES kitchen_tags(id) ON DELETE RESTRICT,
	CONSTRAINT uq_rkts_room_kitchen UNIQUE (room_id, kitchen_tag_id)
);

CREATE INDEX idx_rkts_room_kitchen_tag ON room_kitchen_tag_selections (room_id, kitchen_tag_id);
