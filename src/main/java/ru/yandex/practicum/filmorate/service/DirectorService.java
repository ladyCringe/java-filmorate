package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DirectorService.class);

    @Autowired
    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public DirectorDto add(NewDirectorRequest directorRequest) {
        Director director = DirectorMapper.mapToDirector(directorRequest);

        director = directorDbStorage.add(director);

        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto update(UpdateDirectorRequest directorRequest) {
        Director newDirector = DirectorMapper.mapToDirector(directorRequest);

        newDirector = directorDbStorage.update(newDirector);

        return DirectorMapper.mapToDirectorDto(newDirector);
    }

    public DirectorDto delete(Long directorId) {
        Director removeDirector = directorDbStorage.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + directorId + " не найден."));

        removeDirector = directorDbStorage.delete(removeDirector);

        return DirectorMapper.mapToDirectorDto(removeDirector);
    }

    public Director getById(Long id) {
        return directorDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id = " + id + " не найден."));
    }

    public DirectorDto getByIdDirectorDto(Long id) {
        Director director = directorDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id = " + id + " не найден."));

        return DirectorMapper.mapToDirectorDto(director);
    }

    public Collection<DirectorDto> findAll() {
        return directorDbStorage.findAll().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toList());
    }
}
