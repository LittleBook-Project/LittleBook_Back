package com.littlebook.service;

import com.littlebook.entity.ReadingEntity;
import com.littlebook.entity.UserEntity;
import com.littlebook.entity.BookEntity;
import com.littlebook.enums.ReadingStatus;
import com.littlebook.repository.ReadingRepository;
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
 * Service pour gérer les lectures d'utilisateurs
 */
@Service
public class ReadingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReadingService.class);
    
    @Autowired
    private ReadingRepository readingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;

    /**
     * Ajoute un livre à la liste de lecture d'un utilisateur
     */
    public ReadingEntity addBookToReading(UUID userUuid, String bookIsbn, ReadingStatus status) {
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

        // Vérifie si l'utilisateur n'a pas déjà une lecture pour ce livre
        Optional<ReadingEntity> existingReading = readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn);
        if (existingReading.isPresent()) {
            throw new IllegalArgumentException("Ce livre est déjà dans votre liste de lecture");
        }

        ReadingEntity reading = new ReadingEntity();
        reading.setUser(user.get());
        reading.setBook(book.get());
        reading.setStatus(status);

        ReadingEntity savedReading = readingRepository.save(reading);
        logger.info("Livre {} ajouté à la liste de lecture de l'utilisateur {} avec le statut {}", 
                   bookIsbn, userUuid, status);
        
        return savedReading;
    }

    /**
     * Met à jour le statut de lecture d'un livre
     */
    public ReadingEntity updateReadingStatus(UUID userUuid, String bookIsbn, ReadingStatus newStatus) {
        Optional<ReadingEntity> existingReading = readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn);
        
        if (existingReading.isEmpty()) {
            throw new IllegalArgumentException("Aucune lecture trouvée pour ce livre et cet utilisateur");
        }

        ReadingEntity reading = existingReading.get();
        reading.setStatus(newStatus);

        ReadingEntity updatedReading = readingRepository.save(reading);
        logger.info("Statut de lecture mis à jour pour le livre {} de l'utilisateur {} : {}", 
                   bookIsbn, userUuid, newStatus);
        
        return updatedReading;
    }

    /**
     * Supprime un livre de la liste de lecture
     */
    public void removeBookFromReading(UUID userUuid, String bookIsbn) {
        Optional<ReadingEntity> reading = readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn);
        
        if (reading.isEmpty()) {
            throw new IllegalArgumentException("Aucune lecture trouvée pour ce livre et cet utilisateur");
        }

        readingRepository.delete(reading.get());
        logger.info("Livre {} supprimé de la liste de lecture de l'utilisateur {}", bookIsbn, userUuid);
    }

    /**
     * Récupère toutes les lectures d'un utilisateur
     */
    public List<ReadingEntity> getUserReadings(UUID userUuid) {
        return readingRepository.findByUserUuid(userUuid);
    }

    /**
     * Récupère les lectures d'un utilisateur avec un statut donné
     */
    public List<ReadingEntity> getUserReadingsByStatus(UUID userUuid, ReadingStatus status) {
        return readingRepository.findByUserUuidAndStatus(userUuid, status);
    }

    /**
     * Récupère les livres "À lire" d'un utilisateur
     */
    public List<ReadingEntity> getBooksToRead(UUID userUuid) {
        return getUserReadingsByStatus(userUuid, ReadingStatus.TO_READ);
    }

    /**
     * Récupère les livres "En cours de lecture" d'un utilisateur
     */
    public List<ReadingEntity> getBooksCurrentlyReading(UUID userUuid) {
        return getUserReadingsByStatus(userUuid, ReadingStatus.READING);
    }

    /**
     * Récupère les livres "Lus" d'un utilisateur
     */
    public List<ReadingEntity> getBooksRead(UUID userUuid) {
        return getUserReadingsByStatus(userUuid, ReadingStatus.READ);
    }

    /**
     * Compte le nombre de livres lus par un utilisateur
     */
    public Long getBooksReadCount(UUID userUuid) {
        return readingRepository.countBooksReadByUser(userUuid);
    }

    /**
     * Récupère le statut de lecture d'un livre pour un utilisateur
     */
    public Optional<ReadingStatus> getReadingStatus(UUID userUuid, String bookIsbn) {
        Optional<ReadingEntity> reading = readingRepository.findByUserUuidAndBookIsbn(userUuid, bookIsbn);
        return reading.map(ReadingEntity::getStatus);
    }

    /**
     * Vérifie si un utilisateur a ce livre dans sa liste de lecture
     */
    public boolean hasBookInReading(UUID userUuid, String bookIsbn) {
        return readingRepository.existsByUserUuidAndBookIsbn(userUuid, bookIsbn);
    }
}