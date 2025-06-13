package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        log.info("New request to create review: {}", review);
        Review createdReview = reviewService.createReview(review);
        log.info("New review created: {}", createdReview);
        return createdReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        log.info("New request to update review: {}", review);
        Review updatedReview = reviewService.updateReview(review);
        log.info("Review updated: {}", updatedReview);
        return updatedReview;
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Integer reviewId) {
        log.info("New request to delete review: {}", reviewId);
        reviewService.deleteReview(reviewId);
        log.info("Review deleted");
    }

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10") int count) {
        log.info("Display a list of all movies: {}", reviewService.getAllReviews(filmId, count));
        return reviewService.getAllReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        log.info("Request for review with id {}", id);
        return reviewService.getReviewById(id);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable int reviewId, @PathVariable int userId) {
        log.info("Add like request to review {} by user {}", reviewId, userId);
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable int reviewId, @PathVariable int userId) {
        log.info("Add dislike request to review {} by user {}", reviewId, userId);
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLike(@PathVariable int reviewId, @PathVariable int userId) {
        log.info("Remove like request to review {} by user {}", reviewId, userId);
        reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void removeDislike(@PathVariable int reviewId, @PathVariable int userId) {
        log.info("Remove dislike request to review {} by user {}", reviewId, userId);
        reviewService.removeDislike(reviewId, userId);
    }
}
