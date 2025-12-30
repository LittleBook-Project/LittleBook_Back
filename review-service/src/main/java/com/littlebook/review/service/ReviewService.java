package com.littlebook.review.service;

import com.littlebook.review.entity.ReviewEntity;
import com.littlebook.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository repo;

    public ReviewService(ReviewRepository repo) {
        this.repo = repo;
    }

    public String ping() {
        return "pong";
    }

    // --------- READ / LIST ---------

    public List<ReviewEntity> findAll() {
        return repo.findAll();
    }

    public Optional<ReviewEntity> getById(Long id) {
        return repo.findById(id);
    }

    public List<ReviewEntity> getByBookIsbn(String bookIsbn) {
        return repo.findByBookIsbn(bookIsbn);
    }

    public List<ReviewEntity> getByBookId(String bookId) {
        return repo.findByBookId(bookId);
    }

    public List<ReviewEntity> getByUserUuid(String userUuid) {
        return repo.findByUserUuid(userUuid);
    }

    public Double getAverageRating(String bookIsbn) {
        return repo.getAverageRatingByBookIsbn(bookIsbn);
    }

    public Double getAverageRatingByBookId(String bookId) {
        return repo.getAverageRatingByBookId(bookId);
    }

    public Long getReviewCount(String bookIsbn) {
        return repo.countByBookIsbn(bookIsbn);
    }

    public Long getReviewCountByBookId(String bookId) {
        return repo.countByBookId(bookId);
    }

    // --------- CREATE ---------

    public ReviewEntity create(ReviewEntity r) {
        if (r.getRating() == null || r.getRating() < 1 || r.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (r.getReviewCreationDate() == null) {
            r.setReviewCreationDate(LocalDate.now());
        }

        // Prevent a user from creating more than one review for the same book
        if (r.getUserUuid() != null && r.getBookIsbn() != null) {
            boolean already = repo.existsByUserUuidAndBookIsbn(r.getUserUuid(), r.getBookIsbn());
            if (already) {
                throw new IllegalArgumentException("User has already submitted a review for this book");
            }
        }

        return repo.save(r);
    }

    // --------- UPDATE ---------

    public ReviewEntity update(Long id, ReviewEntity newValues) {
        ReviewEntity existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (newValues.getDescription() != null) {
            existing.setDescription(newValues.getDescription());
        }
        if (newValues.getRating() != null) {
            if (newValues.getRating() < 1 || newValues.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            existing.setRating(newValues.getRating());
        }
        // On ne touche pas userUuid / bookIsbn après création.

        return repo.save(existing);
    }

    // --------- DELETE ---------

    public void delete(Long id, String userUuid) {
        ReviewEntity existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!existing.getUserUuid().equals(userUuid)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        repo.delete(existing);
    }
}
