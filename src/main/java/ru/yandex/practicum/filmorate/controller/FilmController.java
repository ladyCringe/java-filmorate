package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("New request to create film: {}", film);
        Film createdFilm = filmService.createFilm(film);
        log.info("New film created: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("New request to update film: {}", film);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Film updated: {}", updatedFilm);
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Display a list of all movies: {}", filmService.getAllFilms());
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        log.info("Request for film with id {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Add like request to film {} by user {}", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Remove like request to film {} by user {}", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {

        log.info("Get popular films by genre/year/limit: count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam("userId") int userId, @RequestParam("friendId") int friendId) {
        log.info("Поступил запрос на получение общих фильмов у пользователей с id {}.",
                String.valueOf(userId) + "," + String.valueOf(friendId));

        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId,
                                         @RequestParam(name = "sortBy", defaultValue = "year") String sort) {
        log.info("Поступил запрос на получение фильмов режиссера с id {} сортировкой по {}", directorId, sort);

        return filmService.getFilmsByDirector(directorId, sort);
    }

    @GetMapping("/search")
    public List<Film> getFilmsBySearch(@RequestParam(name = "query") String query,
                                       @RequestParam(name = "by") String by) {
        log.info("Поступил запрос на поиск фильмов по вхождению {} в {}.", query, by);

        return filmService.getFilmsBySearch(query, by);
    }

    @DeleteMapping("/{filmId}")
    public Film delete(@PathVariable(name = "filmId") Integer filmIdRequest) {
        log.info("Поступил запрос на удаление фильма с id {}.", filmIdRequest);

        return filmService.delete(filmIdRequest);
    }
}
