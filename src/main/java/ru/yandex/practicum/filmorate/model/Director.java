package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.FieldDescription;
import ru.yandex.practicum.filmorate.validation.Marker;

/**
 * Director.
 */
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Director {
    @NotNull(message = "При обновлении данных о режиссере должен быть указан его id.",
            groups = {Marker.OnUpdate.class, Marker.OnDelete.class})
    @FieldDescription(value = "Уникальный идентификатор режиссера", changeByCopy = false)
    Long id;

    @NotBlank(message = "Имя режиссера не может быть пустым.", groups = Marker.OnCreate.class)
    @FieldDescription("Имя режиссера")
    String name;
}
