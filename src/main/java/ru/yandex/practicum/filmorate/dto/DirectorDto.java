package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.FieldDescription;

@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectorDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @FieldDescription(value = "Уникальный идентификатор режиссера")
    Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @FieldDescription("Имя режиссера")
    String name;
}
