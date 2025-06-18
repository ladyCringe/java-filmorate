package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    private int getNextId() {
        return nextId++;
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film with id = " + film.getId() + " was not found");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Integer id) {
        return films.get(id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        return films.values().stream()
                .filter(film -> genreId == null || film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId)))
                .filter(film -> year == null || film.getReleaseDate().getYear() == year)
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count != null ? count : Integer.MAX_VALUE)
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(int filmId, int userId) {
        getFilmById(filmId).getLikes().add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        getFilmById(filmId).getLikes().remove(userId);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        return List.of();
    }

    @Override
    public Collection<Film> getLikedFilms(int userId) {
        return List.of();
    }

    @Override
    public Collection<Film> getFilmsRecommendations(@PathVariable int userId) {
        return List.of();
    }

    @Override
    public List<Film> getFilmsByDirectorSortByYear(Director director) {
        return List.of();   // в этом хранилище метод не используется
    }

    @Override
    public List<Film> getFilmsByDirectorSortByLikes(Director director) {
        return List.of();   // в этом хранилище метод не используется
    }

    @Override
    public List<Film> getFilmsBySearchInTitle(String query) {
        return List.of();   // в этом хранилище метод не используется
    }

    @Override
    public List<Film> getFilmsBySearchInNameDirector(String query) {
        return List.of();   // в этом хранилище метод не используется
    }

    @Override
    public List<Film> getFilmsBySearchInTitleAndNameDirector(String query) {
        return List.of();   // в этом хранилище метод не используется
    }

    @Override
    public Film delete(Film film) {
        return null;    // в этом хранилище метод не используется
    }
}
