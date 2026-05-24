ALTER TABLE restaurants
	ADD COLUMN name VARCHAR(511),
	ADD COLUMN address VARCHAR(1023),
	ADD COLUMN opening_hours VARCHAR(254),
	ADD COLUMN phone VARCHAR(63),
	ADD COLUMN website_url VARCHAR(511),
	ADD COLUMN latitude DOUBLE PRECISION,
	ADD COLUMN longitude DOUBLE PRECISION;

DELETE FROM restaurant_kitchen_tags;
DELETE FROM restaurants;

/*
INSERT INTO restaurants (id, name, address, opening_hours, phone, website_url, latitude, longitude) VALUES
	(1, 'Итальянский двор', 'Санкт-Петербург, Невский пр., 90', 'Пн–Вс 11:00–23:00', '+7 (812) 000-00-01', 'https://example.com/r1', 59.9332, 30.3434),
	(2, 'Хинкальная на Невском', 'Санкт-Петербург, Невский пр., 112', 'Пн–Вс 10:00–22:00', '+7 (812) 000-00-02', NULL, 59.9318, 30.3601),
	(3, 'Токио City', 'Санкт-Петербург, ул. Белинского, 5', 'Пн–Чт 12:00–23:00, Пт–Вс 12:00–01:00', '+7 (812) 000-00-03', 'https://example.com/r3', 59.9410, 30.3488),
	(4, 'Сычуань', 'Санкт-Петербург, Лиговский пр., 30', 'Пн–Вс 11:00–22:30', '+7 (812) 000-00-04', NULL, 59.9280, 30.3610),
	(5, 'Burger Craft', 'Санкт-Петербург, Рубинштейна, 15', 'Пн–Вс 10:00–00:00', '+7 (812) 000-00-05', 'https://example.com/r5', 59.9265, 30.3455),
	(6, 'У Тайки', 'Санкт-Петербург, ул. Некрасова, 58', 'Вт–Вс 12:00–22:00, Чт–Пн 12:00–23:00', '+7 (812) 000-00-06', NULL, 59.9395, 30.3520),
	(7, 'K-BBQ Корея', 'Санкт-Петербург, Владимирский пр., 19', 'Пн–Вс 11:30–22:30', '+7 (812) 000-00-07', 'https://example.com/r7', 59.9272, 30.3470),
	(8, 'Алаверды', 'Санкт-Петербург, ул. Рубинштейна, 9', 'Пн–Вс 11:00–23:00', '+7 (812) 000-00-08', NULL, 59.9258, 30.3442),
	(9, 'Карш', 'Санкт-Петербург, Загородный пр., 2', 'Пн–Вс 10:00–22:00', '+7 (812) 000-00-09', 'https://example.com/r9', 59.9240, 30.3350),
	(10, 'Curry House', 'Санкт-Петербург, ул. Достоевского, 24', 'Пн–Вс 12:00–22:00', '+7 (812) 000-00-10', NULL, 59.9305, 30.3388),
	(11, 'Taco Loco', 'Санкт-Петербург, Лиговский пр., 47', 'Пн–Чт 12:00–23:00, Пт–Сб 12:00–02:00', '+7 (812) 000-00-11', 'https://example.com/r11', 59.9310, 30.3560),
	(12, 'Bierlin', 'Санкт-Петербург, ул. Марата, 72', 'Пн–Вс 12:00–00:00', '+7 (812) 000-00-12', NULL, 59.9278, 30.3495),
	(13, 'Гастрономика 18', 'Санкт-Петербург, наб. реки Фонтанки, 18', 'Вт–Сб 18:00–23:00', '+7 (812) 000-00-13', 'https://example.com/r13', 59.9325, 30.3410),
	(14, 'Стрит Фуд Маркет', 'Санкт-Петербург, Конногвардейский бульвар, 4', 'Пт–Вс 11:00–22:00', '+7 (812) 000-00-14', NULL, 59.9340, 30.3065),
	(15, 'Зелёная вилка', 'Санкт-Петербург, ул. Чайковского, 2', 'Пн–Вс 10:00–21:00', '+7 (812) 000-00-15', 'https://example.com/r15', 59.9442, 30.3325);
*/

ALTER TABLE restaurants
	ALTER COLUMN name SET NOT NULL,
	ALTER COLUMN address SET NOT NULL,
	ALTER COLUMN opening_hours SET NOT NULL,
	ALTER COLUMN phone SET NOT NULL,
	ALTER COLUMN latitude SET NOT NULL,
	ALTER COLUMN longitude SET NOT NULL;

SELECT setval(
	pg_get_serial_sequence('restaurants', 'id'),
	(SELECT COALESCE(MAX(id), 1) FROM restaurants)
);

/*
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 1, id FROM kitchen_tags WHERE slug IN ('italian', 'european');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 2, id FROM kitchen_tags WHERE slug IN ('georgian', 'caucasus');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 3, id FROM kitchen_tags WHERE slug IN ('japanese', 'pan-asian-cuisine');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 4, id FROM kitchen_tags WHERE slug = 'chinese';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 5, id FROM kitchen_tags WHERE slug IN ('american', 'fast-food');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 6, id FROM kitchen_tags WHERE slug = 'thai';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 7, id FROM kitchen_tags WHERE slug = 'korean';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 8, id FROM kitchen_tags WHERE slug = 'georgian';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 9, id FROM kitchen_tags WHERE slug IN ('middle-eastern', 'caucasus');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 10, id FROM kitchen_tags WHERE slug = 'indian';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 11, id FROM kitchen_tags WHERE slug = 'mexican';
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 12, id FROM kitchen_tags WHERE slug IN ('german', 'european');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 13, id FROM kitchen_tags WHERE slug IN ('gastronomic', 'european');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 14, id FROM kitchen_tags WHERE slug IN ('street-food', 'fast-food');
INSERT INTO restaurant_kitchen_tags (restaurant_id, kitchen_tag_id)
SELECT 15, id FROM kitchen_tags WHERE slug IN ('vegetarian', 'european');
*/
