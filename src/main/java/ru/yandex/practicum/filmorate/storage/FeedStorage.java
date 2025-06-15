package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedStorage {
    void addEvent(FeedEvent event);

    List<FeedEvent> getFeedByUserId(Integer userId);
}
