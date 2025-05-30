package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<MpaRating> getAllRatings() {
        log.info("Request for all MPA ratings");
        return mpaService.getAllRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getRatingById(@PathVariable int id) {
        log.info("Request for MPA rating with id {}", id);
        return mpaService.getRatingById(id);
    }
}