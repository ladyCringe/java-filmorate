package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;
    private final RecommendationService recommendationsService;

    public UserController(UserService userService, RecommendationService recommendationsService) {
        this.userService = userService;
        this.recommendationsService = recommendationsService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("New request to create user: {}", user);
        User createdUser = userService.createUser(user);
        log.info("New user created: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("New request to update user: {}", user);
        User updatedUser = userService.updateUser(user);
        log.info("User updated: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Display a list of all users");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Adding friend {} to user {}", friendId, id);
        userService.addFriend(id, friendId);
        log.info("Friend {} added to user {}", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Removing friend {} from user {}", friendId, id);
        userService.removeFriend(id, friendId);
        log.info("Friend {} removed from user {}", friendId, id);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable int userId) {
        log.info("Запрошены друзья пользователя с id {}", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{friendId}")
    public List<User> getCommonFriends(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Display a list of common friends {} of users {} and {}", userService.getAllUsers(), userId, friendId);
        return userService.getCommonFriends(userId, friendId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getFilmsRecommendations(@PathVariable int id) {
        log.info("Поступил запрос на получение рекомендаций пользователя с id {}.", id);

        return recommendationsService.getFilmsRecommendations(id);
    }

    @DeleteMapping("/{userId}")
    public User delete(@PathVariable(name = "userId") Integer userIdRequest) {
        log.info("Поступил запрос на удаление данных пользователя с id {}.", userIdRequest);

        return userService.delete(userIdRequest);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(name = "id") Integer userId) {
        log.info("Поступил запрос на получение данных пользователя с id {}.", userId);

        return userService.getUserById(userId);
    }
}
