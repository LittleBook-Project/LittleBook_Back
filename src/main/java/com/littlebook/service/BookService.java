package com.littlebook.service;

import com.littlebook.entity.BookEntity;
import com.littlebook.dto.openlibrary.OpenLibraryBookResponse;
import com.littlebook.dto.openlibrary.OpenLibraryBook;
import com.littlebook.repository.BookRepository;
import com.littlebook.service.external.OpenLibraryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service gérant les livres, intégrant la base locale et l’API OpenLibrary.
 */
@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired private BookRepository bookRepository;
    @Autowired private OpenLibraryService openLibraryService;

    /** 🔍 Recherche un livre par ISBN (local → OpenLibrary) */
    public Optional<BookEntity> getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn)
            .or(() -> fetchAndSaveFromOpenLibrary(isbn));
    }

    /** 🔍 Recherche par titre (local → OpenLibrary) */
    public List<BookEntity> searchBooksByName(String title) {
        List<BookEntity> local = bookRepository.findByNameContainingIgnoreCase(title);
        return local.isEmpty() ? mapToEntities(openLibraryService.searchBooksByTitle(title)) : local;
    }

    /** 🔍 Recherche par auteur (local → OpenLibrary) */
    public List<BookEntity> searchBooksByAuthor(String author) {
        List<BookEntity> local = bookRepository.findByAuthorContainingIgnoreCase(author);
        if (!local.isEmpty()) return local;

        List<OpenLibraryBook> olBooks = openLibraryService.searchBooksByAuthor(author);
        return mapToEntities(olBooks);
    }

    /** 💾 Sauvegarde ou met à jour un livre */
    public BookEntity saveBook(BookEntity book) {
        return bookRepository.save(book);
    }

    // --- Méthodes internes propres et réutilisables ---

    /** Importe un livre depuis OpenLibrary par ISBN et le sauvegarde */
    private Optional<BookEntity> fetchAndSaveFromOpenLibrary(String isbn) {
        try {
            OpenLibraryBookResponse resp = openLibraryService.fetchBookByIsbn(isbn);
            OpenLibraryBook olBook = resp != null ? resp.getFirstBook() : null;
            if (olBook == null) return Optional.empty();

            BookEntity book = mapOpenLibraryBook(olBook, isbn);
            return Optional.of(bookRepository.save(book));
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du livre {} : {}", isbn, e.getMessage());
            return Optional.empty();
        }
    }

        /**
     * Vérifie si un ISBN existe sur OpenLibrary
     */
    public boolean isIsbnValidOnOpenLibrary(String isbn) {
        try {
            var resp = openLibraryService.fetchBookByIsbn(isbn);
            return resp != null && resp.getFirstBook() != null;
        } catch (Exception e) {
            logger.warn("Vérification ISBN OpenLibrary échouée : {}", e.getMessage());
            return false;
        }
    }

    /** Convertit une liste OpenLibrary → liste BookEntity */
    private List<BookEntity> mapToEntities(List<OpenLibraryBook> olBooks) {
        List<BookEntity> result = new ArrayList<>();
        for (OpenLibraryBook ol : olBooks) {
            BookEntity b = mapOpenLibraryBook(ol, ol.getFirstIsbn());
            if (b != null) result.add(b);
        }
        return result;
    }

    /** Mappe un OpenLibraryBook → BookEntity */
    private BookEntity mapOpenLibraryBook(OpenLibraryBook olBook, String isbn) {
        if (olBook == null) return null;
        BookEntity book = new BookEntity();
        book.setIsbn(isbn);
        book.setName(olBook.getTitle());
        book.setAuthor(olBook.getFirstAuthorName());
        book.setPublisher(olBook.getFirstPublisherName());
        book.setType(olBook.getBookType());
        book.setCoverImage(olBook.getCoverUrl());
        book.setPublicationDate(parseDate(olBook.getPublishDate()));
        return book;
    }

    /** Convertit une date OpenLibrary vers LocalDate */
    private LocalDate parseDate(String str) {
        if (str == null || str.isBlank()) return null;
        for (String fmt : List.of("yyyy-MM-dd", "yyyy-MM", "yyyy", "MMMM yyyy", "MMMM d, yyyy")) {
            try { return LocalDate.parse(str, DateTimeFormatter.ofPattern(fmt, Locale.ENGLISH)); }
            catch (DateTimeParseException ignored) {}
        }
        try {
            String year = str.replaceAll("\\D", "");
            return (year.length() >= 4) ? LocalDate.of(Integer.parseInt(year.substring(0,4)), 1, 1) : null;
        } catch (Exception e) { return null; }
    }
}

