CREATE TABLE IF NOT EXISTS users
(
    id
    INT
    PRIMARY
    KEY,
    email
    VARCHAR
(
    255
) NOT NULL,
    login VARCHAR
(
    100
) NOT NULL,
    name VARCHAR
(
    100
),
    birthday DATE
    );

CREATE TABLE IF NOT EXISTS mpa_ratings
(
    id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    20
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS films
(
    id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    description VARCHAR
(
    200
),
    release_date DATE,
    duration INT,
    mpa_id INT,
    FOREIGN KEY
(
    mpa_id
) REFERENCES mpa_ratings
(
    id
)
    );

CREATE TABLE IF NOT EXISTS genres
(
    id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    100
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id
    INT
    NOT
    NULL,
    genre_id
    INT
    NOT
    NULL,
    PRIMARY
    KEY
(
    film_id,
    genre_id
),
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    id
),
    FOREIGN KEY
(
    genre_id
) REFERENCES genres
(
    id
)
    );

CREATE TABLE IF NOT EXISTS likes
(
    user_id
    INT
    NOT
    NULL,
    film_id
    INT
    NOT
    NULL,
    PRIMARY
    KEY
(
    user_id,
    film_id
),
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
),
    FOREIGN KEY
(
    film_id
) REFERENCES films
(
    id
)
    );

CREATE TABLE IF NOT EXISTS friendships
(
    user_id
    INT
    NOT
    NULL,
    friend_id
    INT
    NOT
    NULL,
    confirmed
    BOOLEAN,
    PRIMARY
    KEY
(
    user_id,
    friend_id
),
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
),
    FOREIGN KEY
(
    friend_id
) REFERENCES users
(
    id
)
    );
