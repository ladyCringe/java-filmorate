package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int id);

    Review getReviewById(int id);

    List<Review> getAllReviews(Integer filmId, int count);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);
}
