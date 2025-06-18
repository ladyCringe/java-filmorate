package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public FeedService(
            @Qualifier(value = "feedDbStorage") FeedStorage feedStorage,
            @Qualifier(value = "userDbStorage") UserStorage userStorage) {
        this.feedStorage = feedStorage;
        this.userStorage = userStorage;
    }

    public void addEvent(FeedEvent event) {
        validate(event);
        feedStorage.addEvent(event);
    }

    public List<FeedEvent> getFeedByUserId(int userId) {
        return feedStorage.getFeedByUserId(userId);
    }

    private void validate(FeedEvent event) {
        if (event.getEntityId() == null || event.getUserId() == null) {
            throw new ValidationException("No empty userId or entityId allowed");
        }
        if (!(event.getEventType().equals(EventType.LIKE) || event.getEventType().equals(EventType.FRIEND)
                || event.getEventType().equals(EventType.REVIEW))) {
            throw new ValidationException("EventType is incorrect");
        }
        if (!(event.getOperation().equals(Operation.ADD) || event.getOperation().equals(Operation.UPDATE)
                || event.getOperation().equals(Operation.REMOVE))) {
            throw new ValidationException("Operation is incorrect");
        }
    }
}
