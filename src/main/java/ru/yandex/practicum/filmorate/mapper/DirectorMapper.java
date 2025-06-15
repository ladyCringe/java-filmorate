package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {

    public static Director mapToDirector(NewDirectorRequest request) {
        Director director = new Director();
        director.setName(request.getName());

        return director;
    }

    public static Director mapToDirector(UpdateDirectorRequest request) {
        Director director = new Director();
        director.setId(request.getId());
        director.setName(request.getName());

        return director;
    }

    public static DirectorDto mapToDirectorDto(Director director) {
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getId());
        dto.setName(director.getName());

        return dto;
    }
}
