INSERT INTO hotels (id, name, address) VALUES (1, 'Amsterdam Central Hotel', 'Damrak 1, Amsterdam');
INSERT INTO hotels (id, name, address) VALUES (2, 'Canal View Inn', 'Herengracht 10, Amsterdam');

INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (1, 1, '101', TRUE, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (2, 1, '102', TRUE, 1);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (3, 2, '201', TRUE, 0);
INSERT INTO rooms (id, hotel_id, number, available, times_booked) VALUES (4, 2, '202', FALSE, 0);
