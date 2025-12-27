package com.littlebook.review.controller;

import com.littlebook.review.dto.ReviewRequest;
import com.littlebook.review.dto.ReviewResponse;
import com.littlebook.review.entity.ReviewEntity;
import com.littlebook.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    // --- Diagnostics ---

    @GetMapping("/health")
    public String health() {
        return "review-service OK";
    }

    @GetMapping("/ping")
    public String ping() {
        return service.ping();
    }

    // --- CRUD ---

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@RequestBody ReviewRequest req) {
        ReviewEntity entity = mapToEntity(req);
        // on ne fait pas confiance au client pour l'id / date
        entity.setId(null);
        if (entity.getReviewCreationDate() == null) {
            entity.setReviewCreationDate(LocalDate.now());
        }

        ReviewEntity saved = service.create(entity);
        return ResponseEntity
                .created(URI.create("/review/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(r -> ResponseEntity.ok(mapToResponse(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                 @RequestBody ReviewRequest req) {
        try {
            ReviewEntity patch = new ReviewEntity();
            patch.setDescription(req.getDescription());
            patch.setRating(req.getRating());

            ReviewEntity updated = service.update(id, patch);
            return ResponseEntity.ok(mapToResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam String userUuid) {
        try {
            service.delete(id, userUuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(403).build();
        }
    }

    // --- Listing / stats ---

    @GetMapping("/book/{isbn}")
    public List<ReviewResponse> getByBook(@PathVariable("isbn") String bookIsbn) {
        return service.getByBookIsbn(bookIsbn)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/book-id/{bookId}")
    public List<ReviewResponse> getByBookId(@PathVariable String bookId) {
        return service.getByBookId(bookId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/user/{userUuid}")
    public List<ReviewResponse> getByUser(@PathVariable String userUuid) {
        return service.getByUserUuid(userUuid)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/book/{isbn}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable("isbn") String bookIsbn) {
        Double avg = service.getAverageRating(bookIsbn);
        if (avg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(avg);
    }

    @GetMapping("/book/{isbn}/count")
    public ResponseEntity<Long> getReviewCount(@PathVariable("isbn") String bookIsbn) {
        Long count = service.getReviewCount(bookIsbn);
        if (count == null || count == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(count);
    }

    @GetMapping("/book-id/{bookId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByBookId(@PathVariable String bookId) {
        Double avg = service.getAverageRatingByBookId(bookId);
        if (avg == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(avg);
    }

    @GetMapping("/book-id/{bookId}/count")
    public ResponseEntity<Long> getReviewCountByBookId(@PathVariable String bookId) {
        Long count = service.getReviewCountByBookId(bookId);
        if (count == null || count == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(count);
    }

    // --- Mapping helpers ---

    private ReviewEntity mapToEntity(ReviewRequest r) {
        ReviewEntity e = new ReviewEntity();
        e.setDescription(r.getDescription());
        e.setReviewCreationDate(r.getReviewCreationDate());
        e.setRating(r.getRating());
        e.setUserUuid(r.getUserUuid());
        e.setBookIsbn(r.getBookIsbn());
        e.setBookId(r.getBookId());
        return e;
    }

    private ReviewResponse mapToResponse(ReviewEntity e) {
        return new ReviewResponse(
                e.getId(),
                e.getDescription(),
                e.getReviewCreationDate(),
                e.getRating(),
                e.getUserUuid(),
                e.getBookIsbn(),
                e.getBookId()
        );
    }
}
