package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    private final UserStorage userStorage = new InMemoryUserStorage();
    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final UserService userService = new UserService(userStorage);
    private final RecommendationService recommendationService = new RecommendationService(userStorage,filmStorage);
    private final UserController controller = new UserController(userService,recommendationService);
    private User existing;

    @BeforeEach
    void setUp() {
        existing = new User();
        existing.setEmail("test@mail.com");
        existing.setLogin("user1");
        existing.setName("User");
        existing.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldThrowIfEmailIsBlankOrInvalid() {
        existing.setEmail(" ");

        assertThrows(ValidationException.class, () -> controller.createUser(existing));

        existing.setEmail("invalid_email");
        assertThrows(ValidationException.class, () -> controller.createUser(existing));
    }

    @Test
    void shouldThrowIfLoginIsBlankOrWithSpaces() {
        existing.setLogin(" ");
        existing.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.createUser(existing));

        existing.setLogin("user name");
        assertThrows(ValidationException.class, () -> controller.createUser(existing));
    }

    @Test
    void shouldThrowIfBirthdayInFuture() {
        existing.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.createUser(existing));
    }

    @Test
    void shouldUseLoginIfNameIsNull() {
        existing.setName("");
        User created = controller.createUser(existing);

        assertEquals("user1", created.getName());

        existing.setName(null);
        User created2 = controller.createUser(existing);

        assertEquals("user1", created2.getName());
    }

    @Test
    void shouldPassIfUserIsValid() {
        assertDoesNotThrow(() -> controller.createUser(existing));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        controller.createUser(existing);
        User updated = new User();
        updated.setId(existing.getId());
        updated.setEmail("new@mail.com");
        updated.setLogin("newlogin");
        updated.setName("New Name");
        updated.setBirthday(LocalDate.of(1990, 1, 1));

        User result = controller.updateUser(updated);

        assertEquals("new@mail.com", result.getEmail());
        assertEquals("New Name", result.getName());
    }

    @Test
    void shouldThrowIfUserNotFound() {
        controller.createUser(existing);
        User updated = new User();
        updated.setId(999);

        assertThrows(ValidationException.class, () -> controller.updateUser(updated));
    }

    @Test
    void shouldThrowIfEmailInvalid() {
        controller.createUser(existing);
        User updated = new User();
        updated.setId(existing.getId());
        updated.setEmail("no-at-sign");
        updated.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.updateUser(updated));
    }
}
