package com.littlebook.controller;

import com.littlebook.entity.BookEntity;
import com.littlebook.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Récupère un livre par ISBN
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<BookEntity> getBook(@PathVariable String isbn) {
        Optional<BookEntity> book = bookService.getBookByIsbn(isbn);
        return book.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche des livres par nom
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<BookEntity>> searchByTitle(@RequestParam String title) {
        List<BookEntity> books = bookService.searchBooksByName(title);
        return ResponseEntity.ok(books);
    }

    /**
     * Recherche des livres par auteur
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<BookEntity>> searchByAuthor(@RequestParam String author) {
        List<BookEntity> books = bookService.searchBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    /**
     * Ajoute ou met à jour un livre
     */
    @PostMapping
    public ResponseEntity<BookEntity> saveBook(@RequestBody BookEntity book) {
        BookEntity savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    /**
     * Test direct de l'API OpenLibrary (pour debug)
     */
    @GetMapping("/test-openlibrary/{isbn}")
    public ResponseEntity<?> testOpenLibrary(@PathVariable String isbn) {
        try {
            Optional<BookEntity> book = bookService.getBookByIsbn(isbn);
            if (book.isPresent()) {
                return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "source", "OpenLibrary API",
                    "book", book.get()
                ));
            } else {
                return ResponseEntity.ok(java.util.Map.of(
                    "success", false,
                    "message", "Livre non trouvé sur OpenLibrary",
                    "isbn", isbn
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of(
                "success", false,
                "error", e.getMessage(),
                "isbn", isbn
            ));
        }
    }
}
