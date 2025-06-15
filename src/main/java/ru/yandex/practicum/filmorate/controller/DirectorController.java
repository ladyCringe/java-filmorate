package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
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
    public DirectorDto add(@RequestBody @Valid NewDirectorRequest directorRequest) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей

        return directorService.add(directorRequest);
    }

    @PutMapping
    @Validated(Marker.OnUpdate.class)
    public DirectorDto update(@RequestBody @Valid UpdateDirectorRequest directorRequest) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей

        return directorService.update(directorRequest);
    }

    @DeleteMapping("/{id}")
    @Validated(Marker.OnDelete.class)
    public DirectorDto delete(@PathVariable(name = "id") Long directorId) {
        // проверку выполнения необходимых условий осуществил через валидацию полей
        // обработчик выполняется после успешной валидации полей

        return directorService.delete(directorId);
    }

    @GetMapping("/{id}")
    public DirectorDto findById(@PathVariable(name = "id") Long directorId) {
        return directorService.getByIdDirectorDto(directorId);
    }

    @GetMapping
    public Collection<DirectorDto> findAll() {
        return directorService.findAll();
    }
}
