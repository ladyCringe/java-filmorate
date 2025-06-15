package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final FeedService feedService;

    public UserService(@Qualifier(value = "userDbStorage") UserStorage userStorage,
                       @Qualifier(value = "feedService") FeedService feedService) {
        this.userStorage = userStorage;
        this.feedService = feedService;
    }

    public User createUser(User user) {
        validate(user);
        checkName(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        validate(user);
        checkName(user);
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userStorage.getAllUsers());
    }

    public void addFriend(int userId, int friendId) {
        checkUser(userId);
        checkUser(friendId);
        userStorage.addFriend(userId, friendId);
        feedService.addEvent(new FeedEvent(null, null, userId,
                EventType.FRIEND, Operation.ADD, friendId));
    }

    public void removeFriend(int userId, int friendId) {
        checkUser(userId);
        checkUser(friendId);
        userStorage.removeFriend(userId, friendId);
        feedService.addEvent(new FeedEvent(null, null,userId,
                EventType.FRIEND, Operation.REMOVE, friendId));
    }

    public List<User> getFriends(int userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherUserId) {
        User user = getUserById(userId);
        User other = getUserById(otherUserId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
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

    public User getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        return user;
    }

    private void checkUser(int userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }
}
