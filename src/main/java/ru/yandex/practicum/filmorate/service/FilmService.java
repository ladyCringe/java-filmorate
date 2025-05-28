package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film createFilm(Film film) {
        validate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        validate(film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getReleaseDate() == null || film.getDuration() == null) {
            throw new ValidationException("No empty name, release date or duration allowed");
        }
        if (film.getName().isBlank()) {
            throw new ValidationException("Name is required");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description length is longer than 200 symbols");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release date should not be before 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration should be a positive number");
        }
    }

    public void addLike(int filmId, int userId) {
        checkExistence(userId);
        Film film = getFilmById(filmId);
        if (film.getLikes().contains(userId)) {
            throw new ServerException("Film with id " + filmId + " already liked by user " + userId);
        }
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        checkExistence(userId);
        Film film = getFilmById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Like by user with id" + userId +
                    " for film with filmId" + filmId + " was not found");
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count < 0) {
            throw new ServerException("Film count should be greater than 0");
        }
        return filmStorage.getPopularFilms(count);
    }

    private Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Film with id " + id + " not found");
        }
        return film;
    }

    private void checkExistence(Integer userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with id = " + userId + " was not found");
        }
    }
}
