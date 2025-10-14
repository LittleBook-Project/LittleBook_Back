package com.littlebook.service;

import com.littlebook.entity.BookEntity;
import com.littlebook.dto.openlibrary.OpenLibraryBookResponse;
import com.littlebook.dto.openlibrary.OpenLibraryBook;
import com.littlebook.repository.BookRepository;
import com.littlebook.service.external.OpenLibraryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les livres
 * Intègre l'API externe OpenLibrary avec la base de données locale
 */
@Service
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private OpenLibraryService openLibraryService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Récupère un livre par ISBN depuis la base locale ou OpenLibrary
     */
    public Optional<BookEntity> getBookByIsbn(String isbn) {
        // Cherche d'abord dans la base locale
        Optional<BookEntity> localBook = bookRepository.findById(isbn);
        
        if (localBook.isPresent()) {
            logger.debug("Livre trouvé en local pour ISBN: {}", isbn);
            return localBook;
        }
        
        // Si pas trouvé localement, cherche via OpenLibrary
        try {
            String jsonResponse = openLibraryService.fetchBookByIsbn(isbn);
            if (jsonResponse != null && !jsonResponse.trim().equals("{}")) {
                BookEntity bookEntity = mapOpenLibraryToEntity(jsonResponse, isbn);
                if (bookEntity != null) {
                    // Sauvegarde en base pour la prochaine fois
                    bookEntity = bookRepository.save(bookEntity);
                    logger.info("Livre importé depuis OpenLibrary et sauvé: {}", isbn);
                    return Optional.of(bookEntity);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'importation du livre {}: {}", isbn, e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Recherche des livres par nom
     */
    public List<BookEntity> searchBooksByName(String name) {
        return bookRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Recherche des livres par auteur
     */
    public List<BookEntity> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    /**
     * Sauvegarde ou met à jour un livre
     */
    public BookEntity saveBook(BookEntity book) {
        return bookRepository.save(book);
    }

    /**
     * Mappe la réponse JSON d'OpenLibrary vers une entité BookEntity
     */
    private BookEntity mapOpenLibraryToEntity(String jsonResponse, String isbn) {
        try {
            OpenLibraryBookResponse response = objectMapper.readValue(jsonResponse, OpenLibraryBookResponse.class);
            OpenLibraryBook openLibraryBook = response.getFirstBook();
            
            if (openLibraryBook == null) {
                return null;
            }

            BookEntity bookEntity = new BookEntity();
            bookEntity.setIsbn(isbn);
            bookEntity.setName(openLibraryBook.getTitle());
            bookEntity.setAuthor(openLibraryBook.getFirstAuthorName());
            bookEntity.setPublisher(openLibraryBook.getFirstPublisherName());
            
            // Conversion de la date de publication
            if (openLibraryBook.getPublishDate() != null) {
                try {
                    LocalDate pubDate = parsePublishDate(openLibraryBook.getPublishDate());
                    bookEntity.setPublicationDate(pubDate);
                } catch (DateTimeParseException e) {
                    logger.warn("Format de date invalide pour le livre {}: {}", isbn, openLibraryBook.getPublishDate());
                }
            }
            
            // URL de la couverture
            if (openLibraryBook.getCover() != null) {
                bookEntity.setCoverImage(openLibraryBook.getCover().getBestCoverUrl());
            }
            
            // Type/genre basé sur les sujets
            if (openLibraryBook.getSubjects() != null && !openLibraryBook.getSubjects().isEmpty()) {
                bookEntity.setType(openLibraryBook.getSubjects().get(0).getName());
            }

            return bookEntity;
            
        } catch (Exception e) {
            logger.error("Erreur lors du mapping OpenLibrary pour ISBN {}: {}", isbn, e.getMessage());
            return null;
        }
    }

    /**
     * Parse une date de publication depuis OpenLibrary (formats variables)
     */
    private LocalDate parsePublishDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        // Essai de différents formats
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("yyyy"),
            DateTimeFormatter.ofPattern("MMMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM d, yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Continue avec le format suivant
            }
        }
        
        // Si aucun format ne fonctionne, essaye d'extraire juste l'année
        try {
            String year = dateStr.replaceAll("\\D", "");
            if (year.length() >= 4) {
                int yearInt = Integer.parseInt(year.substring(0, 4));
                return LocalDate.of(yearInt, 1, 1);
            }
        } catch (NumberFormatException ignored) {
            // Ignore
        }
        
        throw new DateTimeParseException("Format de date non reconnu", dateStr, 0);
    }
}