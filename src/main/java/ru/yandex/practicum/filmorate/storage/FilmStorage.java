package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Integer id);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getCommonFilms(int userId, int friendId);

    Collection<Film> getLikedFilms(int userId);

    Collection<Film> getFilmsRecommendations(int userId);

    List<Film> getFilmsByDirectorSortByYear(Director director);

    List<Film> getFilmsByDirectorSortByLikes(Director director);

    List<Film> getFilmsBySearchInTitle(String query);

    List<Film> getFilmsBySearchInNameDirector(String query);

    List<Film> getFilmsBySearchInTitleAndNameDirector(String query);

    Film delete(Film film);
}
