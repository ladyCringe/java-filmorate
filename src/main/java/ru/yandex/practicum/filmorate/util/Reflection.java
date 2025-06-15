package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.validation.FieldDescription;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Reflection {
    private Reflection() {
    }

    public static <T> String[] getIgnoreProperties(T obj) {
        List<String> listIgnoreProperties = new ArrayList<>();

        Class<?> filmClass = obj.getClass();
        Field[] fields = filmClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FieldDescription.class)) {
                FieldDescription annotation = field.getAnnotation(FieldDescription.class);
                if (!annotation.changeByCopy()) {
                    listIgnoreProperties.add(field.getName());
                } else {
                    try {
                        field.setAccessible(true);
                        if (field.get(obj) == null) listIgnoreProperties.add(field.getName());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return listIgnoreProperties.toArray(new String[0]);
    }
}
