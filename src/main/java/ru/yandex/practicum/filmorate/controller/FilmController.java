package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    private int getNextId() {
        return nextId++;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("New film created: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Film with id = " + film.getId() + " was not found");
        }
        validate(film);
        films.put(film.getId(), film);
        log.info("Film updated: {}", film);
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getDescription() == null || film.getReleaseDate() == null
                || film.getDuration() == null) {
            throw new ValidationException("No empty fields allowed");
        }
        if (film.getName().isBlank()) {
            throw new ValidationException("Name is required");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Description length is longer than 200 symbols");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release date should not be before 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration should be a positive number");
        }
    }
}
