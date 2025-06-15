package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.FieldDescription;
import ru.yandex.practicum.filmorate.validation.Marker;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewDirectorRequest {
    @NotBlank(message = "Имя режиссера не может быть пустым.", groups = Marker.OnCreate.class)
    @FieldDescription("Имя режиссера")
    String name;
}
