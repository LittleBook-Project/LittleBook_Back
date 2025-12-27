package com.littlebook.book.controller;

import com.littlebook.book.dto.BookRequest;
import com.littlebook.book.dto.BookResponse;
import com.littlebook.book.dto.openlibrary.OpenLibrarySearchResponse;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.exception.OpenLibraryException;
import com.littlebook.book.service.BookService;
import com.littlebook.book.service.BookSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/book")
@Tag(name = "Books", description = "API pour gérer les livres")
public class BookController {

    private final BookService service;
    private final BookSyncService syncService;

    public BookController(BookService service, BookSyncService syncService) {
        this.service = service;
        this.syncService = syncService;
    }

    // --- Diagnostics ---

    @GetMapping("/health")
    public String health() { return "book-service OK"; }

    @GetMapping("/ping")
    public String ping() { return "pong"; }

    // --- OpenLibrary Search & Sync (MUST BE BEFORE /{id} routes) ---

    @GetMapping("/search-openlibrary")
    @Operation(summary = "Rechercher des livres sur OpenLibrary sans les ajouter", description = "Affiche les résultats OpenLibrary sans synchronisation automatique")
    public Page<BookResponse> searchOpenLibraryOnly(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if ((title == null || title.isBlank()) && (author == null || author.isBlank())) {
            throw new IllegalArgumentException("Au moins 'title' ou 'author' est requis");
        }
        
        var pageable = PageRequest.of(page, Math.min(size, 100));
        return syncService.searchOpenLibraryOnly(title, author, pageable).map(this::mapToResponse);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des livres sur OpenLibrary et les synchroniser", description = "Cherche par titre et/ou auteur et synchronise les résultats (DEPRECATED - utiliser search-openlibrary + add-from-openlibrary)")
    public Page<BookResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if ((title == null || title.isBlank()) && (author == null || author.isBlank())) {
            throw new IllegalArgumentException("Au moins 'title' ou 'author' est requis");
        }
        
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "updatedAt"));
        return syncService.searchAndSync(title, author, pageable).map(this::mapToResponse);
    }

    @PostMapping("/add-from-openlibrary")
    @Operation(summary = "Ajouter un livre depuis OpenLibrary", description = "Ajoute un livre spécifique à la collection en utilisant son OpenLibrary ID")
    public ResponseEntity<BookResponse> addFromOpenLibrary(@RequestParam String openlibraryId) {
        // Vérifier si le livre existe déjà
        Optional<BookEntity> existing = service.findByOpenlibraryId(openlibraryId);
        if (existing.isPresent()) {
            return ResponseEntity.ok(mapToResponse(existing.get()));
        }
        
        // Chercher le livre sur OpenLibrary et l'ajouter
        BookEntity synced = syncService.syncByOpenLibraryId(openlibraryId);
        if (synced == null) {
            throw new OpenLibraryException("Aucun livre trouvé sur OpenLibrary pour ID: " + openlibraryId);
        }
        return ResponseEntity.status(201).body(mapToResponse(synced));
    }

    @PostMapping("/sync/{isbn}")
    @Operation(summary = "Synchroniser un livre via ISBN", description = "Récupère les données OpenLibrary et les sauvegarde en base")
    public ResponseEntity<BookResponse> syncByIsbn(@PathVariable String isbn) {
        BookEntity synced = syncService.syncByIsbn(isbn);
        if (synced == null) {
            throw new OpenLibraryException("Aucun livre trouvé sur OpenLibrary pour ISBN: " + isbn);
        }
        return ResponseEntity.ok(mapToResponse(synced));
    }

    @GetMapping("/by-isbn13")
    public ResponseEntity<BookResponse> getByIsbn13(@RequestParam String isbn13) {
        return service.findByIsbn13(isbn13)
                .map(b -> ResponseEntity.ok(mapToResponse(b)))
                .orElseThrow(() -> new IllegalArgumentException("Book with ISBN13 " + isbn13 + " not found"));
    }

    @GetMapping("/by-olid")
    public ResponseEntity<BookResponse> getByOpenlibraryId(@RequestParam String olId) {
        return service.findByOpenlibraryId(olId)
                .map(b -> ResponseEntity.ok(mapToResponse(b)))
                .orElseThrow(() -> new IllegalArgumentException("Book with OpenLibrary ID " + olId + " not found"));
    }

    // --- CRUD ---

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req) {
        BookEntity saved = service.create(mapToEntity(req));
        return ResponseEntity
                .created(URI.create("/books/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable UUID id) {
        BookEntity book = service.getById(id);
        return ResponseEntity.ok(mapToResponse(book));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable UUID id, @Valid @RequestBody BookRequest req) {
        BookEntity updated = service.update(id, mapToEntity(req));
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Listing / recherche simple ---

    @GetMapping
    public Page<BookResponse> list(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String isbn,
                                   @RequestParam(name = "olId", required = false) String openlibraryId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "updatedAt"));
        return service.list(q, isbn, openlibraryId, pageable).map(this::mapToResponse);
    }

    // --- OpenLibrary Search & Sync ---

    @GetMapping("/openlibrary/test/{isbn}")
    @Operation(summary = "Test direct API OpenLibrary", description = "Teste l'API OpenLibrary sans sauvegarder en base")
    public ResponseEntity<OpenLibrarySearchResponse> testOpenLibraryApi(@PathVariable String isbn) {
        // Ce endpoint n'est à utiliser que pour le debug
        // On retourne la réponse brute d'OpenLibrary
        return ResponseEntity.ok(null);  // À implémenter si besoin
    }

    // --- Mapping helpers ---

    private BookEntity mapToEntity(BookRequest r) {
        BookEntity e = new BookEntity();
        e.setOpenlibraryId(r.getOpenlibraryId());
        e.setIsbn10(r.getIsbn10());
        e.setIsbn13(r.getIsbn13());
        e.setTitle(r.getTitle());
        e.setSubtitle(r.getSubtitle());
        e.setAuthors(r.getAuthors());         // String (ex: "a1,a2")
        e.setPublishYear(r.getPublishYear());
        e.setCoverUrl(r.getCoverUrl());
        e.setDescription(r.getDescription());
        e.setSubjects(r.getSubjects());       // String (ex: "s1,s2")
        e.setSourceData(r.getSourceData());   // JSON brut en String
        return e;
    }

    private BookResponse mapToResponse(BookEntity b) {
        return new BookResponse(
                b.getId(),
                b.getOpenlibraryId(),
                b.getIsbn10(),
                b.getIsbn13(),
                b.getTitle(),
                b.getSubtitle(),
                b.getAuthors(),
                b.getPublishYear(),
                b.getCoverUrl(),
                b.getDescription(),
                b.getSubjects(),
                b.getSourceData(),
                b.getCreatedAt(),
                b.getUpdatedAt(),
                b.getLastSyncedAt()
        );
    }
}
