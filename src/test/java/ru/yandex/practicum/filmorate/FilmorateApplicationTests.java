package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void testCreateUser() {
        User u1 = new User();
        u1.setEmail("u1@mail.ru");
        u1.setLogin("u1");
        u1.setName("User One");
        u1.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.createUser(u1);
        assertThat(userStorage.getUserById(103))
                .hasFieldOrPropertyWithValue("id", 103)
                .hasFieldOrPropertyWithValue("email", "u1@mail.ru")
                .hasFieldOrPropertyWithValue("login", "u1")
                .hasFieldOrPropertyWithValue("name", "User One");
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.getUserById(20);
        user.setEmail("new@ya.ru");
        user.setLogin("newlogin");
        user.setName("New Name");
        userStorage.updateUser(user);

        User updated = userStorage.getUserById(20);
        assertThat(updated)
                .hasFieldOrPropertyWithValue("email", "new@ya.ru")
                .hasFieldOrPropertyWithValue("login", "newlogin")
                .hasFieldOrPropertyWithValue("name", "New Name");
    }

    @Test
    void testGetAllUsers() {
        List<User> users = userStorage.getAllUsers();

        assertThat(users.size()).isGreaterThanOrEqualTo(5);
        assertThat(users.stream().anyMatch(user -> user.getId().equals(101))).isTrue();
        assertThat(users.stream().anyMatch(user -> user.getId().equals(102))).isTrue();

    }
}