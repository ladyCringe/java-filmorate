package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FeedService feedService;

    @Autowired
    private DirectorService directorService;

    public FilmService(@Qualifier(value = "filmDbStorage") FilmStorage filmStorage,
                       @Qualifier(value = "userService") UserService userService,
                       @Qualifier(value = "feedService") FeedService feedService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.feedService = feedService;
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

    public List<Film> getFilmsByDirector(Long directorId, String sort) {
        Director director = directorService.getById(directorId);

        if (sort.equals("year")) return filmStorage.getFilmsByDirectorSortByYear(director);
        else if (sort.equals("likes")) return filmStorage.getFilmsByDirectorSortByLikes(director);
        else throw new ValidationException("Не верное значение параметра сортировки при получении фильмов режиссера.");
    }

    public List<Film> getFilmsBySearch(String query, String by) {
        if (by.equals("director")) return filmStorage.getFilmsBySearchInNameDirector(query);
        else if (by.equals("title")) return filmStorage.getFilmsBySearchInTitle(query);
        else if (by.equals("director,title") || by.equals("title,director"))
            return filmStorage.getFilmsBySearchInTitleAndNameDirector(query);
        else
            throw new ValidationException("Не верное значение параметра поиска фильма по наименованию и/или режиссеру.");
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
        User user = userService.getUserById(userId);
        Film film = getFilmById(filmId);
        if (film.getLikes().contains(user.getId())) {
            feedService.addEvent(new FeedEvent(null, null, userId,
                    EventType.LIKE, Operation.ADD, filmId));
            return;
        }
        filmStorage.addLike(filmId, user.getId());
        feedService.addEvent(new FeedEvent(null, null, user.getId(),
                EventType.LIKE, Operation.ADD, filmId));
    }

    public void removeLike(int filmId, int userId) {
        User user = userService.getUserById(userId);
        Film film = getFilmById(filmId);
        if (!film.getLikes().contains(user.getId())) {
            throw new NotFoundException("Like by user with id" + userId +
                    " for film with filmId" + filmId + " was not found");
        }
        filmStorage.removeLike(filmId, user.getId());
        feedService.addEvent(new FeedEvent(null, null, user.getId(),
                EventType.LIKE, Operation.REMOVE, filmId));
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (count != null && count < 0) {
            throw new ServerException("If not null film count should be greater than 0");
        }

        if (year != null) {
            if (year < 1895) throw new ValidationException("Год выпуска фильма не может быть меньше 1895 г.");
            else if (year > LocalDate.now().getYear())
                throw new ValidationException("Год выпуска фильма не может быть больше текущего года.");
        }

        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public Film delete(Integer filmIdRequest) {
        Film removeFilm = filmStorage.getFilmById(filmIdRequest);

        removeFilm = filmStorage.delete(removeFilm);

        return removeFilm;
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Пользователь и друг не могут быть одним и тем же человеком");
        }
        userService.getUserById(userId);
        userService.getUserById(friendId);
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        return commonFilms != null ? commonFilms : Collections.emptyList();
    }
}
