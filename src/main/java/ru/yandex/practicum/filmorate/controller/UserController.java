package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    private int getNextId() {
        return nextId++;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        validate(user);
        checkName(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("New user created: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("User with id = " + user.getId() + " was not found");
        }
        validate(user);
        checkName(user);
        users.put(user.getId(), user);
        log.info("User updated: {}", user);
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Display a list of all users: {}", users);
        return new ArrayList<>(users.values());
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email should not be empty and must contain @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Login should not be empty nor should space contain");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthdate should be in the past");
        }
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
