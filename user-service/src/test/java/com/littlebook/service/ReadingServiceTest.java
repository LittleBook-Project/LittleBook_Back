package com.littlebook.service;

import com.littlebook.entity.BookEntity;
import com.littlebook.entity.ReadingEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.enums.ReadingStatus;
import com.littlebook.repository.ReadingRepository;
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
 * Tests unitaires pour ReadingService
 */
@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ReadingService readingService;

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
    void addBookToReading_Success() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(true);
        when(bookService.getBookByIsbn(bookIsbn)).thenReturn(Optional.of(testBook));
        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn)).thenReturn(Optional.empty());
        
        ReadingEntity savedReading = new ReadingEntity();
        savedReading.setId(1L);
        savedReading.setStatus(ReadingStatus.READING);
        savedReading.setUser(testUser);
        savedReading.setBook(testBook);
        
        when(readingRepository.save(any(ReadingEntity.class))).thenReturn(savedReading);

        // When
        ReadingEntity result = readingService.addBookToReading(userUuid, bookIsbn, ReadingStatus.READING);

        // Then
        assertNotNull(result);
        assertEquals(ReadingStatus.READING, result.getStatus());
        verify(readingRepository, times(1)).save(any(ReadingEntity.class));
    }

    @Test
    void addBookToReading_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.addBookToReading(userUuid, bookIsbn, ReadingStatus.READING)
        );
        
        assertEquals("Utilisateur introuvable", exception.getMessage());
        verify(readingRepository, never()).save(any());
    }

    @Test
    void addBookToReading_InvalidIsbn_ThrowsException() {
        // Given
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.addBookToReading(userUuid, bookIsbn, ReadingStatus.READING)
        );
        
        assertEquals("ISBN inexistant sur OpenLibrary. Impossible d'ajouter une lecture pour un livre inconnu.", exception.getMessage());
    }

    @Test
    void addBookToReading_AlreadyInList_ThrowsException() {
        // Given
        ReadingEntity existingReading = new ReadingEntity();
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(testUser));
        when(bookService.isIsbnValidOnOpenLibrary(bookIsbn)).thenReturn(true);
        when(bookService.getBookByIsbn(bookIsbn)).thenReturn(Optional.of(testBook));
        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn))
            .thenReturn(Optional.of(existingReading));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.addBookToReading(userUuid, bookIsbn, ReadingStatus.READING)
        );
        
        assertEquals("Ce livre est déjà dans votre liste de lecture", exception.getMessage());
    }

    @Test
    void updateReadingStatus_Success() {
        // Given
        ReadingEntity existingReading = new ReadingEntity();
        existingReading.setStatus(ReadingStatus.READING);

        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn))
            .thenReturn(Optional.of(existingReading));
        when(readingRepository.save(any(ReadingEntity.class))).thenReturn(existingReading);

        // When
        ReadingEntity result = readingService.updateReadingStatus(userUuid, bookIsbn, ReadingStatus.READ);

        // Then
        assertNotNull(result);
        assertEquals(ReadingStatus.READ, result.getStatus());
        verify(readingRepository, times(1)).save(existingReading);
    }

    @Test
    void updateReadingStatus_NotFound_ThrowsException() {
        // Given
        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn))
            .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.updateReadingStatus(userUuid, bookIsbn, ReadingStatus.READ)
        );
        
        assertEquals("Aucune lecture trouvée pour ce livre et cet utilisateur", exception.getMessage());
    }

    @Test
    void removeBookFromReading_Success() {
        // Given
        ReadingEntity existingReading = new ReadingEntity();
        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn))
            .thenReturn(Optional.of(existingReading));

        // When
        readingService.removeBookFromReading(userUuid, bookIsbn);

        // Then
        verify(readingRepository, times(1)).delete(existingReading);
    }

    @Test
    void removeBookFromReading_NotFound_ThrowsException() {
        // Given
        when(readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn))
            .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.removeBookFromReading(userUuid, bookIsbn)
        );
        
        assertEquals("Aucune lecture trouvée pour ce livre et cet utilisateur", exception.getMessage());
    }
}
