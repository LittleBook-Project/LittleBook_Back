package com.littlebook.service;

import com.littlebook.entity.ReviewEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.entity.BookEntity;
import com.littlebook.repository.ReviewRepository;
import com.littlebook.repository.UserRepository;
import com.littlebook.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les reviews de livres
 */
@Service
public class ReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;

    /**
     * Crée une nouvelle review
     */
    public ReviewEntity createReview(UUID userUuid, String bookIsbn, String description, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }

        // Vérifie si l'utilisateur existe
        Optional<UserEntity> user = userRepository.findById(userUuid);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable");
        }

        // Vérifie si le livre existe
        Optional<BookEntity> book = bookRepository.findById(bookIsbn);
        if (book.isEmpty()) {
            throw new IllegalArgumentException("Livre introuvable");
        }

        // Vérifie si l'utilisateur n'a pas déjà reviewé ce livre
        if (reviewRepository.existsByUserUuidAndBookIsbn(userUuid, bookIsbn)) {
            throw new IllegalArgumentException("Vous avez déjà reviewé ce livre");
        }

        ReviewEntity review = new ReviewEntity();
        review.setUser(user.get());
        review.setBook(book.get());
        review.setDescription(description);
        review.setRating(rating);

        ReviewEntity savedReview = reviewRepository.save(review);
        logger.info("Nouvelle review créée pour le livre {} par l'utilisateur {}", bookIsbn, userUuid);
        
        return savedReview;
    }

    /**
     * Met à jour une review existante
     */
    public ReviewEntity updateReview(Long reviewId, String description, Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }

        Optional<ReviewEntity> existingReview = reviewRepository.findById(reviewId);
        if (existingReview.isEmpty()) {
            throw new IllegalArgumentException("Review introuvable");
        }

        ReviewEntity review = existingReview.get();
        
        if (description != null) {
            review.setDescription(description);
        }
        
        if (rating != null) {
            review.setRating(rating);
        }

        return reviewRepository.save(review);
    }

    /**
     * Supprime une review
     */
    public void deleteReview(Long reviewId, UUID userUuid) {
        Optional<ReviewEntity> review = reviewRepository.findById(reviewId);
        if (review.isEmpty()) {
            throw new IllegalArgumentException("Review introuvable");
        }

        // Vérifie que l'utilisateur est bien l'auteur de la review
        if (!review.get().getUser().getUuid().equals(userUuid)) {
            throw new IllegalArgumentException("Vous ne pouvez supprimer que vos propres reviews");
        }

        reviewRepository.deleteById(reviewId);
        logger.info("Review {} supprimée par l'utilisateur {}", reviewId, userUuid);
    }

    /**
     * Récupère toutes les reviews d'un livre
     */
    public List<ReviewEntity> getReviewsByBook(String isbn) {
        return reviewRepository.findByBookIsbn(isbn);
    }

    /**
     * Récupère toutes les reviews d'un utilisateur
     */
    public List<ReviewEntity> getReviewsByUser(UUID userUuid) {
        return reviewRepository.findByUserUuid(userUuid);
    }

    /**
     * Calcule la note moyenne d'un livre
     */
    public Double getAverageRating(String isbn) {
        return reviewRepository.getAverageRatingByBookIsbn(isbn);
    }

    /**
     * Compte le nombre de reviews d'un livre
     */
    public Long getReviewCount(String isbn) {
        return reviewRepository.countByBookIsbn(isbn);
    }

    /**
     * Récupère une review par ID
     */
    public Optional<ReviewEntity> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }
}