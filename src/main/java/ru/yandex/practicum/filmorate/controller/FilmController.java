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
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Get popular films: {}", filmService.getPopularFilms(count));
        return filmService.getPopularFilms(count);
    }
}
