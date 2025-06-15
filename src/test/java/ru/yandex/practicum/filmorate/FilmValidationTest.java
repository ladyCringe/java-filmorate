package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private final UserStorage userStorage = new InMemoryUserStorage();
    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final UserService userService = new UserService(userStorage);
    private final FilmService filmService = new FilmService(filmStorage, userStorage, userService);
    private final FilmController controller = new FilmController(filmService);
    private Film existing;

    @BeforeEach
    void setUp() {
        existing = new Film();
        existing.setName("Old");
        existing.setDescription("Old desc");
        existing.setReleaseDate(LocalDate.of(2000, 1, 1));
        existing.setDuration(100);
    }

    @Test
    void shouldThrowIfNameIsBlank() {
        existing.setName(" ");

        assertThrows(ValidationException.class, () -> controller.createFilm(existing));
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        existing.setDescription("A".repeat(201));

        assertThrows(ValidationException.class, () -> controller.createFilm(existing));
    }

    @Test
    void shouldThrowIfDateBefore1895() {
        existing.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class, () -> controller.createFilm(existing));
    }

    @Test
    void shouldThrowIfDurationIsNegative() {
        existing.setDuration(-5);

        assertThrows(ValidationException.class, () -> controller.createFilm(existing));
        existing.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.createFilm(existing));
    }

    @Test
    void shouldPassIfFilmIsValid() {
        assertDoesNotThrow(() -> controller.createFilm(existing));
    }

    @Test
    void shouldUpdateFilmSuccessfully() {
        controller.createFilm(existing);
        Film updated = new Film();
        updated.setId(existing.getId());
        updated.setName("New name");
        updated.setDescription("New desc");
        updated.setReleaseDate(LocalDate.of(2020, 1, 1));
        updated.setDuration(120);

        Film result = controller.updateFilm(updated);

        assertEquals("New name", result.getName());
        assertEquals("New desc", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    void shouldThrowIfFilmNotFound() {
        controller.createFilm(existing);
        Film updated = new Film();
        updated.setId(999);

        assertThrows(ValidationException.class, () -> controller.updateFilm(updated));
    }

    @Test
    void shouldThrowIfDurationNegativeOnUpdate() {
        controller.createFilm(existing);
        Film updated = new Film();
        updated.setId(existing.getId());
        updated.setDuration(-50);

        assertThrows(ValidationException.class, () -> controller.updateFilm(updated));
    }
}
