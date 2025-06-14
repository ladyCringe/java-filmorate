INSERT INTO PUBLIC.USERS (ID, EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES (1, 'a@mail.com', 'lol', null, '2025-05-16');

INSERT INTO PUBLIC.USERS (ID, EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES (101, 'u1@mail.ru', 'u1', 'User One', '2000-01-01');

INSERT INTO PUBLIC.USERS (ID, EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES (102, 'u2@mail.ru', 'u2', 'User Two', '2001-02-02');

INSERT INTO PUBLIC.USERS (ID, EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES (20, 'old@ya.ru', 'oldlogin', 'Old Name', '1990-01-01');

INSERT INTO PUBLIC.USERS (ID, EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES (10, 'test@ya.ru', 'test', 'Test User', '1995-05-10');

INSERT INTO Directors (id, name)
VALUES (1, 'Режиссер 1'),
       (2, 'Режиссер 2'),
       (3, 'Режиссер 3');

INSERT INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (1, 'Interstellar', 'Space epic', '2014-11-07', 169, 1);

INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 1);

INSERT INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (2, 'Old Name', 'Old Desc', '2000-01-01', 100, 1);

INSERT INTO film_genres (film_id, genre_id)
VALUES (2, 1);


INSERT INTO films (id, name, description, release_date, duration, mpa_id)
VALUES (4, 'Film A', 'A desc', '2010-05-05', 100, 1),
       (3, 'Film B', 'B desc', '2011-06-06', 110, 1);

INSERT INTO film_genres (film_id, genre_id)
VALUES (4, 1),
       (3, 2);







