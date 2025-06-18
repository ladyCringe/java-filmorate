package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Collection;

@Service
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DirectorService.class);

    @Autowired
    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public Director add(Director directorRequest) {
        return directorDbStorage.add(directorRequest);
    }

    public Director update(Director directorRequest) {
        return directorDbStorage.update(directorRequest);
    }

    public Director delete(Long directorId) {
        Director removeDirector = getByIdDirector(directorId);

        return directorDbStorage.delete(removeDirector);
    }

    public Director getById(Long id) {
        return getByIdDirector(id);
    }

    private Director getByIdDirector(Long id) {
        return directorDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id = " + id + " не найден."));
    }

    public Collection<Director> findAll() {
        return directorDbStorage.findAll();
    }
}
