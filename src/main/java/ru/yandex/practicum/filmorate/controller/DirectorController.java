package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DirectorController.class);

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public Director add(@RequestBody @Valid Director directorRequest) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей
        log.info("Поступил запрос на добавление режиссера {}.", directorRequest);

        return directorService.add(directorRequest);
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public Director update(@RequestBody @Valid Director directorRequest) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей
        log.info("Поступил запрос на обновление режиссера {}.", directorRequest);

        return directorService.update(directorRequest);
    }

    @DeleteMapping("/{id}")
    @Validated(Marker.OnDelete.class)
    public Director delete(@PathVariable(name = "id") Long directorId) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей
        log.info("Поступил запрос на удаление режиссера с id {}.", directorId);

        return directorService.delete(directorId);
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable(name = "id") Long directorId) {
        log.info("Поступил запрос на получение данных по режиссеру с id {}.", directorId);

        return directorService.getById(directorId);
    }

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Поступил запрос на получение данных по всем режиссерам.");

        return directorService.findAll();
    }
}
