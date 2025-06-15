package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/{userId}/feed")
    public List<FeedEvent> getFeedByUserId(@PathVariable Integer userId) {
        log.info("Request for feed for user with id {}", userId);
        return feedService.getFeedByUserId(userId);
    }
}
