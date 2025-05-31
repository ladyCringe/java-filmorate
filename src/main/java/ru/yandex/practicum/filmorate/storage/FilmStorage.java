package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Integer id);

    List<Film> getPopularFilms(int count);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}
