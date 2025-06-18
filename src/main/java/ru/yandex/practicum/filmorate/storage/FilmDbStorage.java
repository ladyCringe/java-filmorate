package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.extractors.FilmsResultSetExtractor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private static final String TOP_PART_QUERY_FILMS = "SELECT\n" +
            "tt_f.ID AS f_id,\n" +
            "tt_f.NAME AS f_name,\n" +
            "tt_f.DESCRIPTION AS f_description,\n" +
            "tt_f.RELEASE_DATE AS f_release_date,\n" +
            "tt_f.DURATION AS f_duration,\n" +
            "tt_f.MPA_ID AS mpa_id,\n" +
            "tt_mr.NAME AS mr_name,\n" +
            "tt_d.ID AS director_id,\n" +
            "tt_d.NAME AS director_name,\n" +
            "tt_l.USER_ID AS likes_user_id,\n" +
            "tt_fg.GENRE_ID AS genre_id,\n" +
            "tt_g.NAME AS genre_name\n";
    private static final String TABLE_PARTS_QUERY_FILMS = "\n" +
            "LEFT JOIN MPA_RATINGS tt_mr ON tt_mr.ID = tt_f.MPA_ID\n" +
            "LEFT JOIN DIRECTORS tt_d ON tt_d.ID = tt_fd.DIRECTOR_ID\n" +
            "LEFT JOIN LIKES tt_l ON tt_l.FILM_ID = tt_f.ID\n" +
            "LEFT JOIN FILM_GENRES tt_fg ON tt_fg.FILM_ID = tt_f.ID\n" +
            "LEFT JOIN GENRES tt_g ON tt_g.ID = tt_fg.GENRE_ID\n";
    private static final String FIND_BY_DIRECTOR_SORT_BY_YEAR = TOP_PART_QUERY_FILMS +
            "FROM FILMS tt_f\n" +
            "INNER JOIN FILM_DIRECTOR tt_fd ON tt_fd.FILM_ID  = tt_f.ID AND tt_fd.director_id = :director_id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY\n" +
            "tt_f.RELEASE_DATE,\n" +
            "tt_f.ID;\n";
    private static final String FIND_BY_DIRECTOR_SORT_BY_LIKES = "--запрос с несколькими with не сработал в jdbc\n" +
            TOP_PART_QUERY_FILMS +
            "FROM films tt_f\n" +
            "INNER JOIN\n" +
            "(\n" +
            "\tSELECT film_isLike.id AS film_id, sum(film_isLike.isLike) AS sumLike\n" +
            "\tFROM\n" +
            "\t(\n" +
            "\t\tSELECT f.id,\n" +
            "\t\t\tCASE\n" +
            "\t\t\t\tWHEN l.film_id IS NULL THEN 0\n" +
            "\t\t\t\tELSE 1\n" +
            "\t\t\tEND AS isLike\n" +
            "\t\tFROM FILMS f\n" +
            "\t\tINNER JOIN FILM_DIRECTOR fd ON fd.film_id = f.id AND fd.director_id = :director_id\n" +
            "\t\tLEFT JOIN LIKES l ON l.film_id = f.id\n" +
            "\t) AS film_isLike\n" +
            "\tGROUP BY film_isLike.id) AS film_sumLike\n" +
            "ON film_sumLike.film_id = tt_f.id\n" +
            "LEFT JOIN FILM_DIRECTOR tt_fd ON tt_fd.film_id = tt_f.id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY film_sumLike.sumLike DESC;\n";    //в задании сортировать по годам, в тестах обратный порядок
    private static final String FIND_BY_SEARCH_IN_TITLE = "--запрос с несколькими with не сработал в jdbc\n" +
            TOP_PART_QUERY_FILMS +
            "FROM films tt_f\n" +
            "INNER JOIN\n" +
            "(\n" +
            "\tSELECT film_isLike.film_id AS film_id, sum(film_isLike.isLike) AS sumLike\n" +
            "\tFROM\n" +
            "\t(\n" +
            "\t\tSELECT f_search.film_id,\n" +
            "\t\tCASE\n" +
            "\t\t\tWHEN l.film_id IS NULL THEN 0\n" +
            "\t\t\tELSE 1\n" +
            "\t\tEND AS isLike\n" +
            "\t\tFROM\n" +
            "\t\t(\n" +
            "\t\t\tSELECT f.id AS film_id\n" +
            "\t\t\tFROM FILMS f\n" +
            "\t\t\tWHERE f.name ILIKE concat('%', :searchQuery, '%')\n" +
            "\t\t) AS f_search\n" +
            "\t\tLEFT JOIN LIKES l ON l.film_id = f_search.film_id\n" +
            "\t) AS film_isLike\n" +
            "\tGROUP BY film_isLike.film_id\n" +
            ") AS film_sumLike\n" +
            "ON film_sumLike.film_id = tt_f.id\n" +
            "LEFT JOIN FILM_DIRECTOR tt_fd ON tt_fd.film_id = tt_f.id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String FIND_BY_SEARCH_IN_DIRECTOR_NAME = "--запрос с несколькими with не сработал в jdbc\n" +
            TOP_PART_QUERY_FILMS +
            "FROM films tt_f\n" +
            "INNER JOIN\n" +
            "(\n" +
            "\tSELECT film_isLike.film_id AS film_id, sum(film_isLike.isLike) AS sumLike\n" +
            "\tFROM\n" +
            "\t(\n" +
            "\t\tSELECT fd.film_id,\n" +
            "\t\tCASE\n" +
            "\t\t\tWHEN l.film_id IS NULL THEN 0\n" +
            "\t\t\tELSE 1\n" +
            "\t\tEND AS isLike\n" +
            "\t\tFROM\n" +
            "\t\t(\n" +
            "\t\t\tSELECT d.id AS director_id\n" +
            "\t\t\tFROM DIRECTORS AS d\n" +
            "\t\t\tWHERE d.name ILIKE concat('%', :searchQuery, '%')\n" +
            "\t\t) AS d_search\n" +
            "\t\tINNER JOIN FILM_DIRECTOR AS fd ON fd.director_id = d_search.director_id\n" +
            "\t\tLEFT JOIN LIKES l ON l.film_id = fd.film_id\n" +
            "\t) AS film_isLike\n" +
            "\tGROUP BY film_isLike.film_id\n" +
            ") AS film_sumLike\n" +
            "ON film_sumLike.film_id = tt_f.id\n" +
            "LEFT JOIN FILM_DIRECTOR tt_fd ON tt_fd.film_id = tt_f.id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String FIND_BY_SEARCH_IN_TITLE_AND_DIRECTOR_NAME = "--запрос с несколькими with не сработал в jdbc\n" +
            TOP_PART_QUERY_FILMS +
            "FROM films tt_f\n" +
            "INNER JOIN\n" +
            "(\n" +
            "\tSELECT film_isLike.film_id AS film_id, sum(film_isLike.isLike) AS sumLike\n" +
            "\tFROM\n" +
            "\t(\n" +
            "\t\tSELECT f_search.film_id,\n" +
            "\t\tCASE\n" +
            "\t\t\tWHEN l.film_id IS NULL THEN 0\n" +
            "\t\t\tELSE 1\n" +
            "\t\tEND AS isLike\n" +
            "\t\tFROM\n" +
            "\t\t(\n" +
            "\t\t\tSELECT fd.film_id\n" +
            "\t\t\tFROM\n" +
            "\t\t\t(\n" +
            "\t\t\t\tSELECT d.id AS director_id\n" +
            "\t\t\t\tFROM DIRECTORS AS d\n" +
            "\t\t\t\tWHERE d.name ILIKE concat('%', :searchQuery, '%')\n" +
            "\t\t\t) AS d_search\n" +
            "\t\t\tINNER JOIN FILM_DIRECTOR AS fd ON fd.director_id = d_search.director_id\n" +
            "\t\t\t\n" +
            "\t\t\tUNION\n" +
            "\t\t\t\n" +
            "\t\t\tSELECT f_search.film_id\n" +
            "\t\t\tFROM\n" +
            "\t\t\t(\n" +
            "\t\t\t\tSELECT f.id AS film_id\n" +
            "\t\t\t\tFROM FILMS f\n" +
            "\t\t\t\tWHERE f.name ILIKE concat('%', :searchQuery, '%')\n" +
            "\t\t\t) AS f_search\n" +
            "\t\t) AS f_search\n" +
            "\t\tLEFT JOIN LIKES l ON l.film_id = f_search.film_id\n" +
            "\t) AS film_isLike\n" +
            "\tGROUP BY film_isLike.film_id\n" +
            ") AS film_sumLike\n" +
            "ON film_sumLike.film_id = tt_f.id\n" +
            "LEFT JOIN FILM_DIRECTOR tt_fd ON tt_fd.film_id = tt_f.id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String DELETE_FILM_IN_DIRECTORS_QUERY = "DELETE FROM film_director WHERE film_id = :film_id;";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = :film_id;";
    private static final String DELETE_FILM_LIKES_QUERY = "DELETE FROM likes WHERE film_id = :film_id;";
    private static final String DELETE_FILM_IN_REVIEWS_LIKES = "DELETE FROM REVIEW_LIKES\n" +
            "WHERE review_id IN\n" +
            "\t(SELECT id FROM Reviews WHERE film_id = :film_id);\t--удаляем оценки отзывов к фильму\n";
    private static final String DELETE_FILM_IN_REVIEWS = "DELETE FROM REVIEWS WHERE film_id = :film_id;";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = :film_id;";
    private static final String FIND_COMMON = "\n" +
            TOP_PART_QUERY_FILMS +
            "FROM films tt_f\n" +
            "INNER JOIN\n" +
            "(\n" +
            "SELECT f.id,\n" +
            "COUNT(DISTINCT fl.user_id) AS popularity\n" +
            "FROM films f\n" +
            "JOIN likes fl ON f.id = fl.film_id\n" +
            "WHERE fl.user_id IN (?, ?)\n" +
            "GROUP BY f.id\n" +
            "HAVING COUNT(DISTINCT fl.user_id) = 2) AS filmPopularity\n" +
            "ON filmPopularity.id = tt_f.id\n" +
            "LEFT JOIN FILM_DIRECTOR tt_fd ON tt_fd.film_id = tt_f.id\n" +
            TABLE_PARTS_QUERY_FILMS +
            "ORDER BY popularity DESC\n";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DirectorDbStorage directorDbStorage;

    @Autowired
    FilmsResultSetExtractor filmsResultSetExtractor;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null && !mpaExistsById(film.getMpa().getId())) {
            throw new NotFoundException("Mpa does not exist");
        }

        if (!film.getGenres().isEmpty() && !genresExistById(film.getGenres())) {
            throw new NotFoundException("Genres do not exist");
        }

        List<Long> longListDirectors = film.getDirectors().stream()
                .map(Director::getId)
                .filter(id -> id != 0)
                .toList();
        Collection<Director> directorCollection = directorDbStorage.getAllByParameterId(longListDirectors);
        if (longListDirectors.size() != directorCollection.size())
            throw new NotFoundException("Не найдены режиссеры по списку.");
        film.setDirectors(new HashSet<>(directorCollection));

        Integer nextId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM films", Integer.class
        );
        film.setId(nextId);

        String sql = "INSERT INTO films (id, name, description, release_date, duration, mpa_id)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() == null ? 1 : film.getMpa().getId());

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
        List<Genre> newGenres = film.getGenres().stream().sorted(Comparator.comparing(Genre::getId)).toList();
        film.setGenres(new LinkedHashSet<>(newGenres));

        // установим режиссеров фильма
        final String insertFilmDirectorsQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?);";
        jdbcTemplate.batchUpdate(insertFilmDirectorsQuery, getBatchPreparedStatementSetter(film));

        return film;
    }

    private static BatchPreparedStatementSetter getBatchPreparedStatementSetter(Film film) {
        return new BatchPreparedStatementSetter() {
            final List<Director> directorList = new ArrayList<>(film.getDirectors());

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = directorList.get(i);
                ps.setObject(1, film.getId());
                ps.setObject(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return directorList.size();
            }
        };
    }

    @Override
    public Film updateFilm(Film film) {
        if (film == null || film.getId() == null || !filmExistsById(film.getId())) {
            throw new NotFoundException("Film does not exist");
        }

        if (film.getMpa() != null && !mpaExistsById(film.getMpa().getId())) {
            throw new NotFoundException("Mpa does not exist");
        }

        if (!genresExistById(film.getGenres())) {
            throw new NotFoundException("Genres do not exist");
        }

        List<Long> longListDirectors = film.getDirectors().stream()
                .map(Director::getId)
                .filter(id -> id != 0)
                .toList();
        Collection<Director> directorCollection = directorDbStorage.getAllByParameterId(longListDirectors);
        if (longListDirectors.size() != directorCollection.size())
            throw new NotFoundException("Не найдены режиссеры по списку.");

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?," +
                " duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }

        // обновляем данные о режиссерах
        final String deleteFilmGenresQuery = "DELETE FROM film_director WHERE film_id = ?;";
        final String insertFilmGenresQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?);";
        jdbcTemplate.update(deleteFilmGenresQuery, film.getId());   // удаляем записи о текущих режиссерах фильма
        film.setDirectors(new HashSet<>(directorCollection));
        jdbcTemplate.batchUpdate(insertFilmGenresQuery, getBatchPreparedStatementSetter(film)); // вставляем записи о новых режиссерах
        List<Genre> newGenres = film.getGenres().stream().sorted(Comparator.comparing(Genre::getId)).toList();
        film.setGenres(new LinkedHashSet<>(newGenres));

        return film;
    }

    public List<Film> getAllFilms() {
        String filmSql = """
                    SELECT f.*, m.name AS mpa_name
                    FROM films f
                    JOIN mpa_ratings m ON f.mpa_id = m.id
                """;

        List<Film> films = jdbcTemplate.query(filmSql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new MpaRating(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            film.setDirectors(new HashSet<>(directorDbStorage.getDirectorsByFilm(film)));   // режиссеры фильма
            return film;
        });

        Map<Integer, Set<Genre>> genresMap = getGenresForFilms();
        Map<Integer, Set<Integer>> likesMap = getLikesForFilms();

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.getLikes().addAll(likesMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    @Override
    public Collection<Film> getFilmsRecommendations(int userId) {
        String sql = """
        SELECT f.*
        FROM films f
        JOIN (
            SELECT l.film_id
            FROM likes l
            JOIN (
                SELECT l.user_id
                FROM likes l
                JOIN (
                    SELECT film_id
                    FROM likes
                    WHERE user_id = ?
                ) ulf ON l.film_id = ulf.film_id
                WHERE l.user_id <> ?
                GROUP BY l.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 1
            ) su ON l.user_id = su.user_id
            WHERE l.film_id NOT IN (
                SELECT film_id
                FROM likes
                WHERE user_id = ?
            )
        ) recommended_film_ids ON f.id = recommended_film_ids.film_id;
        """;

        log.info("searching for recommendations for user with id = {}", userId);

        return jdbcTemplate.query(sql, this::mapRowToFilm, userId, userId, userId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortByYear(Director director) {
        log.info("Запрошены фильмы по режиссеру {} с сортировкой по году выпуска.", director);

        SqlParameterSource parameters = new MapSqlParameterSource("director_id", director.getId());

        return namedParameterJdbcTemplate.query(FIND_BY_DIRECTOR_SORT_BY_YEAR, parameters, filmsResultSetExtractor);
    }

    @Override
    public List<Film> getFilmsByDirectorSortByLikes(Director director) {
        log.info("Запрошены фильмы по режиссеру {} с сортировкой по лайкам (убывание).", director);

        SqlParameterSource parameters = new MapSqlParameterSource("director_id", director.getId());

        return namedParameterJdbcTemplate.query(FIND_BY_DIRECTOR_SORT_BY_LIKES, parameters, filmsResultSetExtractor);
    }

    @Override
    public List<Film> getFilmsBySearchInTitle(String query) {
        log.info("Запрошены фильмы в наименовании которых есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_TITLE, parameters, filmsResultSetExtractor);
    }

    @Override
    public List<Film> getFilmsBySearchInNameDirector(String query) {
        log.info("Запрошены фильмы, у которых в имени режиссеров есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_DIRECTOR_NAME, parameters, filmsResultSetExtractor);
    }

    @Override
    public List<Film> getFilmsBySearchInTitleAndNameDirector(String query) {
        log.info("Запрошены фильмы, у которых в наименовании или имени режиссеров есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_TITLE_AND_DIRECTOR_NAME, parameters, filmsResultSetExtractor);
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!filmExistsById(id)) {
            throw new NotFoundException("Film with id " + id + " not found");
        }
        return jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", this::mapRowToFilm, id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                    SELECT f.*, m.name AS mpa_name
                    FROM films f
                    JOIN mpa_ratings m ON f.mpa_id = m.id
                    LEFT JOIN likes l ON f.id = l.film_id
                """);

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.id = fg.film_id ");
        }

        sql.append("WHERE 1=1 ");

        if (genreId != null) {
            sql.append("AND fg.genre_id = ? ");
            params.add(genreId);
        }

        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        sql.append("""
                    GROUP BY f.id, m.name
                    ORDER BY COUNT(l.user_id) DESC
                    LIMIT ?
                """);

        params.add(count != null ? count : Integer.MAX_VALUE);

        List<Film> films = jdbcTemplate.query(sql.toString(), this::mapRowToFilm, params.toArray());

        Map<Integer, Set<Genre>> genresByFilmId = getGenresForFilms();

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private Map<Integer, Set<Genre>> getGenresForFilms() {
        return jdbcTemplate.query("""
                    SELECT fg.film_id, g.id, g.name
                    FROM film_genres fg
                    JOIN genres g ON fg.genre_id = g.id
                """, rs -> {
            Map<Integer, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return map;
        });
    }

    private Map<Integer, Set<Integer>> getLikesForFilms() {
        return jdbcTemplate.query("""
                    SELECT film_id, user_id
                    FROM likes
                """, rs -> {
            Map<Integer, Set<Integer>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                int userId = rs.getInt("user_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        log.info("setting id");
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        log.info("searching for mpa with id = {}", rs.getInt("mpa_id"));

        MpaRating mpa = jdbcTemplate.queryForObject(
                "SELECT * FROM mpa_ratings WHERE id = ?",
                (mprs, i) -> new MpaRating(mprs.getInt("id"), mprs.getString("name")),
                rs.getInt("mpa_id")
        );
        film.setMpa(mpa);

        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.* FROM genres g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ?",
                (grs, i) -> new Genre(grs.getInt("id"), grs.getString("name")),
                film.getId()
        );
        film.setGenres(new HashSet<>(genres));

        List<Integer> likes = jdbcTemplate.query(
                "SELECT user_id FROM likes WHERE film_id = ?",
                (lrs, i) -> lrs.getInt("user_id"),
                film.getId()
        );
        film.getLikes().addAll(likes);

        film.setDirectors(new HashSet<>(directorDbStorage.getDirectorsByFilm(film)));   // режиссеры фильма

        return film;
    }

    private boolean filmExistsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM FILMS WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }

    private boolean mpaExistsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM MPA_RATINGS WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }

    private boolean genresExistById(Set<Genre> genres) {
        for (Genre genre : genres) {
            if (genre.getId() == 0) {
                continue;
            }
            String sql = "SELECT COUNT(*) FROM GENRES WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genre.getId());
            if (count <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        return jdbcTemplate.query(FIND_COMMON, filmsResultSetExtractor, userId, friendId);
    }

    public Collection<Film> getLikedFilms(int userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        List<Integer> filmIds = jdbcTemplate.queryForList(sql, Integer.class, userId);
        Collection<Film> likedFilms = new HashSet<>();
        for (Integer filmId : filmIds) {
            try {
                Film film = getFilmById(filmId);
                likedFilms.add(film);
            } catch (NotFoundException e) {
                log.warn("Фильм с ID {} был лайкнут, но не найден в БД", filmId);
            }
        }
        return likedFilms;
    }

    @Override
    @Transactional
    public Film delete(Film film) {
        SqlParameterSource parameters = new MapSqlParameterSource("film_id", film.getId());

        // удаляем данные о режиссерах фильма
        namedParameterJdbcTemplate.update(DELETE_FILM_IN_DIRECTORS_QUERY, parameters);

        // удаляем данные о жанрах фильма
        namedParameterJdbcTemplate.update(DELETE_FILM_GENRES_QUERY, parameters);

        // удаляем данные о лайках фильма
        namedParameterJdbcTemplate.update(DELETE_FILM_LIKES_QUERY, parameters);

        // удаляем данные об оценках к отзывам фильма
        namedParameterJdbcTemplate.update(DELETE_FILM_IN_REVIEWS_LIKES, parameters);

        // удаляем отзывы к фильму
        namedParameterJdbcTemplate.update(DELETE_FILM_IN_REVIEWS, parameters);

        // удаляем данные о фильме из БД.
        namedParameterJdbcTemplate.update(DELETE_FILM_QUERY, parameters);

        return film;
    }
}
