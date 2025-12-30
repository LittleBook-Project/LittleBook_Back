package com.littlebook.controller;

import com.littlebook.entity.ReviewEntity;
import com.littlebook.dto.ReviewDTO;
import com.littlebook.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les reviews de livres
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Crée une nouvelle review pour un livre
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody CreateReviewRequest request) {
        try {
            ReviewEntity review = reviewService.createReview(
                request.getUserUuid(), 
                request.getBookIsbn(), 
                request.getDescription(), 
                request.getRating()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(review));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Met à jour une review existante
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId, 
            @RequestBody UpdateReviewRequest request) {
        try {
            ReviewEntity review = reviewService.updateReview(
                reviewId, 
                request.getDescription(), 
                request.getRating()
            );
            return ResponseEntity.ok(toDTO(review));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprime une review
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId, 
            @RequestParam UUID userUuid) {
        try {
            reviewService.deleteReview(reviewId, userUuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère une review par ID
     * GET /api/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReview(@PathVariable Long reviewId) {
        Optional<ReviewEntity> review = reviewService.getReviewById(reviewId);
        return review.map(r -> ResponseEntity.ok(toDTO(r)))
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère toutes les reviews d'un livre
     * GET /api/reviews/book/{isbn}
     */
    @GetMapping("/book/{isbn}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBook(@PathVariable String isbn) {
        List<ReviewEntity> reviews = reviewService.getReviewsByBook(isbn);
        return ResponseEntity.ok(reviews.stream().map(this::toDTO).toList());
    }

    /**
     * Récupère toutes les reviews (endpoint global, utile pour debug)
     * GET /api/reviews
     */
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewEntity> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews.stream().map(this::toDTO).toList());
    }

    /**
     * Récupère toutes les reviews d'un utilisateur
     * GET /api/reviews/user/{userUuid}
     */
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable UUID userUuid) {
        List<ReviewEntity> reviews = reviewService.getReviewsByUser(userUuid);
        return ResponseEntity.ok(reviews.stream().map(this::toDTO).toList());
    }

    // --- Méthode utilitaire pour transformer une ReviewEntity en ReviewDTO ---
    private ReviewDTO toDTO(ReviewEntity entity) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setReviewCreationDate(entity.getReviewCreationDate());
        dto.setRating(entity.getRating());
        if (entity.getUser() != null && entity.getUser().getUuid() != null) {
            dto.setUserUuid(entity.getUser().getUuid().toString());
        }
        if (entity.getBook() != null && entity.getBook().getIsbn() != null) {
            dto.setBookIsbn(entity.getBook().getIsbn());
        }
        return dto;
    }

    /**
     * Récupère les statistiques d'un livre (note moyenne et nombre de reviews)
     * GET /api/reviews/book/{isbn}/stats
     */
    @GetMapping("/book/{isbn}/stats")
    public ResponseEntity<BookStatsResponse> getBookStats(@PathVariable String isbn) {
        Double averageRating = reviewService.getAverageRating(isbn);
        Long reviewCount = reviewService.getReviewCount(isbn);
        
        BookStatsResponse stats = new BookStatsResponse(
            averageRating != null ? averageRating : 0.0, 
            reviewCount
        );
        
        return ResponseEntity.ok(stats);
    }

    // --- Classes internes pour les DTOs ---

    /**
     * DTO pour la création d'une review
     */
    public static class CreateReviewRequest {
        private UUID userUuid;
        private String bookIsbn;
        private String description;
        private Integer rating;

        // Getters & Setters
        public UUID getUserUuid() { return userUuid; }
        public void setUserUuid(UUID userUuid) { this.userUuid = userUuid; }

        public String getBookIsbn() { return bookIsbn; }
        public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }

    /**
     * DTO pour la mise à jour d'une review
     */
    public static class UpdateReviewRequest {
        private String description;
        private Integer rating;

        // Getters & Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }

    /**
     * DTO pour les statistiques d'un livre
     */
    public static class BookStatsResponse {
        private Double averageRating;
        private Long reviewCount;

        public BookStatsResponse(Double averageRating, Long reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        // Getters & Setters
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getReviewCount() { return reviewCount; }
        public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }
    }
}