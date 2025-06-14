
package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(@Qualifier(value = "reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier(value = "filmDbStorage") FilmStorage filmStorage,
                         @Qualifier(value = "userDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review createReview(Review review) {
        validateReview(review);
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        validateReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int id) {
        validateReview(getReviewById(id));
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(int id) {
        Review review = reviewStorage.getReviewById(id);
        if (review == null) {
            throw new NotFoundException("review with id " + id + " not found");
        }
        return review;
    }

    public List<Review> getAllReviews(Integer filmId, int count) {
        return reviewStorage.getAllReviews(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        validateReview(getReviewById(reviewId));
        checkUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        validateReview(getReviewById(reviewId));
        checkUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        validateReview(getReviewById(reviewId));
        checkUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        validateReview(getReviewById(reviewId));
        checkUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getUserId() == null) {
            throw new ValidationException("User with id " + review.getUserId() + " not found");
        }
        if (userStorage.getUserById(review.getUserId()) == null) {
            throw new ValidationException("User with id " + review.getUserId() + " not found");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Film with id " + review.getFilmId() + " not found");
        }
        if (filmStorage.getFilmById(review.getFilmId()) == null) {
            throw new ValidationException("Film with id " + review.getFilmId() + " not found");
        }
    }

    private void checkUserExists(int userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }
}
