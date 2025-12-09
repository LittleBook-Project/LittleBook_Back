package com.littlebook.book.service;

import com.littlebook.book.dto.openlibrary.OpenLibraryBook;
import com.littlebook.book.dto.openlibrary.OpenLibrarySearchResponse;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.service.external.OpenLibraryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de synchronisation des livres avec OpenLibrary
 * Enrichit la base de données avec les données d'OpenLibrary
 */
@Service
public class BookSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSyncService.class);
    
    private final BookService bookService;
    private final OpenLibraryClient openLibraryClient;
    
    public BookSyncService(BookService bookService, OpenLibraryClient openLibraryClient) {
        this.bookService = bookService;
        this.openLibraryClient = openLibraryClient;
    }
    
    /**
     * Synchronise un livre depuis OpenLibrary par ISBN
     * @param isbn ISBN-10 ou ISBN-13
     * @return Le livre créé ou mis à jour
     */
    public BookEntity syncByIsbn(String isbn) {
        logger.info("Syncing book from OpenLibrary for ISBN: {}", isbn);
        
        // Chercher un livre existant
        Optional<BookEntity> existing = bookService.findByIsbn13(isbn)
                .or(() -> bookService.findByIsbn13(isbn.replace("-", "")));
        
        if (existing.isPresent()) {
            logger.info("Book already exists with ISBN: {}, updating lastSyncedAt", isbn);
            BookEntity book = existing.get();
            book.setLastSyncedAt(LocalDateTime.now());
            return bookService.update(book.getId(), book);
        }
        
        // Chercher sur OpenLibrary
        OpenLibrarySearchResponse response = openLibraryClient.searchByIsbn(isbn);
        
        if (response == null || response.getDocs() == null || response.getDocs().isEmpty()) {
            logger.warn("No book found on OpenLibrary for ISBN: {}", isbn);
            return null;
        }
        
        OpenLibraryBook olBook = response.getDocs().get(0);
        BookEntity newBook = mapOpenLibraryToEntity(olBook);
        newBook.setLastSyncedAt(LocalDateTime.now());
        
        return bookService.create(newBook);
    }
    
    /**
     * Recherche des livres sur OpenLibrary et les sauvegarde en base
     * @param title Titre du livre
     * @param author Auteur du livre
     * @param pageable Pagination
     * @return Page de livres
     */
    public Page<BookEntity> searchAndSync(String title, String author, Pageable pageable) {
        logger.info("Searching and syncing books: title={}, author={}", title, author);
        
        OpenLibrarySearchResponse response = openLibraryClient.search(title, author, null, pageable.getPageSize());
        
        if (response == null || response.getDocs() == null || response.getDocs().isEmpty()) {
            logger.warn("No books found on OpenLibrary for title={}, author={}", title, author);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        List<BookEntity> books = new ArrayList<>();
        
        for (OpenLibraryBook olBook : response.getDocs()) {
            try {
                // Vérifier si le livre existe déjà par ISBN13
                String isbn13 = (olBook.getIsbn13() != null && !olBook.getIsbn13().isEmpty())
                        ? olBook.getIsbn13().get(0)
                        : null;
                
                Optional<BookEntity> existing = Optional.empty();
                
                if (isbn13 != null) {
                    existing = bookService.findByIsbn13(isbn13);
                }
                
                // Si pas trouvé par ISBN, vérifier par OpenLibrary ID
                if (existing.isEmpty() && olBook.getKey() != null) {
                    String olId = olBook.getKey().replace("/works/", "");
                    existing = bookService.findByOpenlibraryId(olId);
                }
                
                if (existing.isPresent()) {
                    books.add(existing.get());
                    continue;
                }
                
                // Créer un nouveau livre
                BookEntity newBook = mapOpenLibraryToEntity(olBook);
                newBook.setLastSyncedAt(LocalDateTime.now());
                BookEntity saved = bookService.create(newBook);
                books.add(saved);
                
            } catch (Exception e) {
                logger.error("Error syncing book: {}", olBook.getTitle(), e);
            }
        }
        
        return new PageImpl<>(books, pageable, response.getNumFound());
    }
    
    /**
     * Recherche par titre seulement
     */
    public Page<BookEntity> searchAndSyncByTitle(String title, Pageable pageable) {
        return searchAndSync(title, null, pageable);
    }
    
    /**
     * Recherche par auteur seulement
     */
    public Page<BookEntity> searchAndSyncByAuthor(String author, Pageable pageable) {
        return searchAndSync(null, author, pageable);
    }
    
    /**
     * Convertit un livre OpenLibrary en BookEntity
     */
    private BookEntity mapOpenLibraryToEntity(OpenLibraryBook olBook) {
        BookEntity book = new BookEntity();
        
        book.setOpenlibraryId(olBook.getOpenLibraryId());
        book.setTitle(olBook.getTitle());
        book.setSubtitle(olBook.getSubtitle());
        book.setAuthors(olBook.getAuthorsAsString());
        book.setPublishYear(olBook.getFirstPublishYear());
        book.setCoverUrl(olBook.getCoverUrl());
        book.setDescription(olBook.getDescription());
        book.setSubjects(olBook.getSubjectsAsString());
        
        // ISBN
        if (olBook.getIsbn13() != null && !olBook.getIsbn13().isEmpty()) {
            book.setIsbn13(olBook.getIsbn13().get(0));
        }
        if (olBook.getIsbn() != null && !olBook.getIsbn().isEmpty()) {
            book.setIsbn10(olBook.getIsbn().get(0));
        }
        
        // Sauvegarder les données brutes
        book.setSourceData(toJson(olBook));
        
        return book;
    }
    
    /**
     * Convertit un objet en JSON (format simplifié)
     */
    private String toJson(OpenLibraryBook olBook) {
        return "{\"openLibraryId\":\"" + olBook.getOpenLibraryId() + 
               "\",\"title\":\"" + escape(olBook.getTitle()) + 
               "\",\"authors\":\"" + escape(olBook.getAuthorsAsString()) + "\"}";
    }
    
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
