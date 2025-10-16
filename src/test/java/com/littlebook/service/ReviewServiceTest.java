package com.littlebook.service;

import com.littlebook.entity.BookEntity;
import com.littlebook.entity.ReviewEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.repository.ReviewRepository;
import com.littlebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ReviewService
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userUuid;
    private String bookIsbn;
    private UserEntity testUser;
    private BookEntity testBook;

    @BeforeEach
    void setUp() {
        userUuid = UUID.randomUUID();
        bookIsbn = "9780439708180";

        testUser = new UserEntity();
        testUser.setUuid(userUuid);
        testUser.setUserName("john_doe");

        testBook = new BookEntity();
        testBook.setIsbn(bookIsbn);
        testBook.setName("Harry Potter");
    }

    @Test
    void createReview_Success() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(true);
        when(bookService.getBookByIsbn(bookIsbn)).thenReturn(Optional.of(testBook));
        when(reviewRepository.existsByUserUuidAndBookIsbn(userUuid, bookIsbn)).thenReturn(false);
        
        ReviewEntity savedReview = new ReviewEntity();
        savedReview.setId(1L);
        savedReview.setRating(5);
        savedReview.setDescription("Great book!");
        savedReview.setUser(testUser);
        savedReview.setBook(testBook);
        
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(savedReview);

        // When
        ReviewEntity result = reviewService.createReview(userUuid, bookIsbn, "Great book!", 5);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great book!", result.getDescription());
        verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
    }

    @Test
    void createReview_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(userUuid, bookIsbn, "Great book!", 5)
        );
        
        assertEquals("Utilisateur introuvable", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_InvalidRating_ThrowsException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(userUuid, bookIsbn, "Great book!", 6)
        );
        
        assertEquals("La note doit être entre 1 et 5", exception.getMessage());
    }

    @Test
    void createReview_IsbnNotValid_ThrowsException() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(userUuid, bookIsbn, "Great book!", 5)
        );
        
        assertEquals("ISBN inexistant sur OpenLibrary. Impossible de créer une review pour un livre inconnu.", exception.getMessage());
    }

    @Test
    void createReview_AlreadyReviewed_ThrowsException() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(true);
        when(bookService.getBookByIsbn(bookIsbn)).thenReturn(Optional.of(testBook));
        when(reviewRepository.existsByUserUuidAndBookIsbn(userUuid, bookIsbn)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.createReview(userUuid, bookIsbn, "Great book!", 5)
        );
        
        assertEquals("Vous avez déjà reviewé ce livre", exception.getMessage());
    }

    @Test
    void updateReview_Success() {
        // Given
        Long reviewId = 1L;
        ReviewEntity existingReview = new ReviewEntity();
        existingReview.setId(reviewId);
        existingReview.setRating(3);
        existingReview.setDescription("Original description");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(existingReview);

        // When
        ReviewEntity result = reviewService.updateReview(reviewId, "Updated description", 5);

        // Then
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(existingReview);
    }

    @Test
    void updateReview_NotFound_ThrowsException() {
        // Given
        Long reviewId = 999L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.updateReview(reviewId, "Updated", 5)
        );
        
        assertEquals("Review introuvable", exception.getMessage());
    }

    @Test
    void deleteReview_Success() {
        // Given
        Long reviewId = 1L;
        ReviewEntity existingReview = new ReviewEntity();
        existingReview.setId(reviewId);
        existingReview.setUser(testUser);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        // When
        reviewService.deleteReview(reviewId, userUuid);

        // Then
        verify(reviewRepository, times(1)).deleteById(reviewId);
    }

    @Test
    void deleteReview_NotOwner_ThrowsException() {
        // Given
        Long reviewId = 1L;
        UUID otherUserUuid = UUID.randomUUID();
        ReviewEntity existingReview = new ReviewEntity();
        existingReview.setId(reviewId);
        existingReview.setUser(testUser);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewService.deleteReview(reviewId, otherUserUuid)
        );
        
        assertEquals("Vous ne pouvez supprimer que vos propres reviews", exception.getMessage());
        verify(reviewRepository, never()).deleteById(anyLong());
    }
}
