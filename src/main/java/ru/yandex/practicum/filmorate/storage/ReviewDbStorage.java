
package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {
        Integer nextId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM reviews", Integer.class
        );
        review.setReviewId(nextId);

        jdbcTemplate.update(
                "INSERT INTO reviews (id, content, is_positive, user_id, film_id, usefulness)" +
                        " VALUES (?, ?, ?, ?, ?, 0)",
                review.getReviewId(), review.getContent(), review.getIsPositive(),
                review.getUserId(), review.getFilmId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(
                "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?",
                review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(int id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
    }

    @Override
    public Review getReviewById(int id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, id);
        if (reviews.isEmpty()) {
            throw new NotFoundException("Review with id " + id + " not found");
        }
        return reviews.getFirst();
    }

    @Override
    public List<Review> getAllReviews(Integer filmId, int count) {
        if (filmId != null) {
            return jdbcTemplate.query(
                    "SELECT * FROM reviews WHERE film_id = ? ORDER BY usefulness DESC LIMIT ?",
                    this::mapRowToReview, filmId, count);
        } else {
            return jdbcTemplate.query(
                    "SELECT * FROM reviews ORDER BY usefulness DESC LIMIT ?",
                    this::mapRowToReview, count);
        }
    }

    @Override
    public void addLike(int reviewId, int userId) {
        if (checkLikeExists(reviewId, userId)) {
            return;
        }

        removeDislike(reviewId, userId);
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_positive) " +
                "VALUES (?, ?, true)", reviewId, userId);
        jdbcTemplate.update("UPDATE reviews SET usefulness = usefulness + 1 WHERE id = ?", reviewId);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        if (checkDislikeExists(reviewId, userId)) {
            return;
        }

        removeLike(reviewId, userId);
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_positive) " +
                "VALUES (?, ?, false)", reviewId, userId);
        jdbcTemplate.update("UPDATE reviews SET usefulness = usefulness - 1 WHERE id = ?", reviewId);
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        if (!checkLikeExists(reviewId, userId)) {
            return;
        }

        jdbcTemplate.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ? " +
                "AND is_positive = true", reviewId, userId);
        jdbcTemplate.update("UPDATE reviews SET usefulness = usefulness - 1 WHERE id = ?", reviewId);
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        if (!checkDislikeExists(reviewId, userId)) {
            return;
        }

        jdbcTemplate.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ? " +
                "AND is_positive = false", reviewId, userId);
        jdbcTemplate.update("UPDATE reviews SET usefulness = usefulness + 1 WHERE id = ?", reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("usefulness"))
                .build();
    }

    private boolean checkDislikeExists(int reviewId, int userId) {
        String sql = "SELECT COUNT(*) FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND IS_POSITIVE = false";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        return count > 0;
    }

    private boolean checkLikeExists(int reviewId, int userId) {
        String sql = "SELECT COUNT(*) FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND IS_POSITIVE = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        return count > 0;
    }
}
