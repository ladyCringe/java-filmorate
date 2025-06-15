package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(@Qualifier("userDbStorage") UserStorage userStorage,
                                 @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getFilmsRecommendations(@PathVariable int userId) {
        Collection<User> users = userStorage.getAllUsers();
        Set<Integer> userFilmIds = filmStorage.getLikedFilms(userId).stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        User similarLikesUser;

        Map<User, Set<Integer>> userLikes = new HashMap<>();
        for (User user : users) {
            if (userId == user.getId()) {
                continue;
            }
            userLikes.put(user, filmStorage.getLikedFilms(user.getId()).stream()
                    .map(Film::getId).collect(Collectors.toSet()));
        }

        Map<User, Long> userSimilarity = users.stream()
                .filter(user -> user.getId() != userId)
                .collect(Collectors.toMap(
                        user -> user,
                        user -> {
                            Set<Integer> otherUserFilmIds = filmStorage.getLikedFilms(user.getId()).stream()
                                    .map(Film::getId)
                                    .collect(Collectors.toSet());
                            return userFilmIds.stream()
                                    .filter(otherUserFilmIds::contains)
                                    .count();
                        }
                ));

        Optional<User> optSimilarLikesUser = userSimilarity.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst();

        if (optSimilarLikesUser.isEmpty()) {
            return Collections.emptyList();
        } else {
            similarLikesUser = optSimilarLikesUser.get();
        }

        Set<Integer> similarUserLikes = userLikes.get(similarLikesUser);

        Set<Integer> diff = new HashSet<>(similarUserLikes);
        diff.removeAll(userFilmIds);

        return diff.stream()
                .map(filmStorage::getFilmById)
                .collect(Collectors.toCollection(HashSet::new));
    }
}