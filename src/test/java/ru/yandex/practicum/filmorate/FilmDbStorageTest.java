package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Test
    void testCreateAndGetFilmById() {
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Space epic");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);
        film.setMpa(new MpaRating(1, "PG-13"));
        film.setGenres(new HashSet<Genre>() {{
            add(new Genre(2, "Comedy"));
        }});
        filmStorage.createFilm(film);

        Film loaded = filmStorage.getAllFilms().getLast();

        Film result = filmStorage.getFilmById(loaded.getId());
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Interstellar");
        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getMpa().getId()).isEqualTo(1);
        assertThat(result.getGenres()).hasSize(1);
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.getFilmById(2);
        film.setName("New Name");
        film.setDescription("Updated Desc");
        film.setDuration(120);
        film.setMpa(new MpaRating(2, "R"));
        film.setGenres(new HashSet<>(List.of(new Genre(2, "Comedy"))));
        filmStorage.updateFilm(film);

        Film updated = filmStorage.getFilmById(2);
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Desc");
        assertThat(updated.getMpa().getId()).isEqualTo(2);
        assertThat(updated.getGenres()).anyMatch(g -> g.getId() == 2);
    }

    @Test
    void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        assertThat(films.size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void testGetPopularFilms() {
        List<Film> popular = filmStorage.getPopularFilms(2);
        assertThat(popular.size()).isGreaterThanOrEqualTo(2);
    }
}
