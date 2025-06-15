package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_BY_SEARCH_IN_TITLE = "--запрос с несколькими with не сработал в jdbc\n" +
            "SELECT *\n" +
            "FROM films\n" +
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
            "ON film_sumLike.film_id = films.id\n" +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String FIND_BY_SEARCH_IN_DIRECTOR_NAME = "--запрос с несколькими with не сработал в jdbc\n" +
            "SELECT *\n" +
            "FROM films\n" +
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
            "ON film_sumLike.film_id = films.id\n" +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String FIND_BY_SEARCH_IN_TITLE_AND_DIRECTOR_NAME = "--запрос с несколькими with не сработал в jdbc\n" +
            "SELECT *\n" +
            "FROM films\n" +
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
            "ON film_sumLike.film_id = films.id\n" +
            "ORDER BY film_sumLike.sumLike DESC;\n";
    private static final String FIND_BY_DIRECTOR_SORT_BY_LIKES = "--запрос с несколькими with не сработал в jdbc\n" +
            "SELECT *\n" +
            "FROM films\n" +
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
            "\t\tINNER JOIN FILM_DIRECTOR fd ON fd.film_id = f.id AND fd.director_id = ?\n" +
            "\t\tLEFT JOIN LIKES l ON l.film_id = f.id\n" +
            "\t) AS film_isLike\n" +
            "\tGROUP BY film_isLike.id) AS film_sumLike\n" +
            "ON film_sumLike.film_id = films.id\n" +
            "ORDER BY film_sumLike.sumLike DESC;\n";    //в задании сортировать по годам, в тестах обратный порядок

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DirectorDbStorage directorDbStorage;

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
    public List<Film> getFilmsByDirectorSortByYear(Director director) {
        final String query = "SELECT * FROM FILMS f" +
                " INNER JOIN FILM_DIRECTOR fd ON f.id = fd.film_id AND fd.director_id = ?" +
                " ORDER BY f.RELEASE_DATE;";

        return jdbcTemplate.query(query, this::mapRowToFilm, director.getId());
    }

    @Override
    public List<Film> getFilmsByDirectorSortByLikes(Director director) {
        log.info("Запрошены фильмы по режиссеру {}.", director);

        return jdbcTemplate.query(FIND_BY_DIRECTOR_SORT_BY_LIKES, this::mapRowToFilm, director.getId());
    }

    @Override
    public List<Film> getFilmsBySearchInTitle(String query) {
        log.info("Запрошены фильмы в наименовании которых есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_TITLE, parameters, this::mapRowToFilm);
    }

    @Override
    public List<Film> getFilmsBySearchInNameDirector(String query) {
        log.info("Запрошены фильмы, у которых в имени режиссеров есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_DIRECTOR_NAME, parameters, this::mapRowToFilm);
    }

    @Override
    public List<Film> getFilmsBySearchInTitleAndNameDirector(String query) {
        log.info("Запрошены фильмы, у которых в наименовании или имени режиссеров есть {}.", query);

        SqlParameterSource parameters = new MapSqlParameterSource("searchQuery", query);

        return namedParameterJdbcTemplate.query(FIND_BY_SEARCH_IN_TITLE_AND_DIRECTOR_NAME, parameters, this::mapRowToFilm);
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
        String sql = "SELECT f.*, COUNT(DISTINCT fl.user_id) AS popularity " +
                "FROM films f " +
                "JOIN likes fl ON f.id = fl.film_id " +
                "WHERE fl.user_id IN (?, ?) " +
                "GROUP BY f.id " +
                "HAVING COUNT(DISTINCT fl.user_id) = 2 " +
                "ORDER BY popularity DESC";

        return jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);
    }
}
