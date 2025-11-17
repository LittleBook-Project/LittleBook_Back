package com.littlebook.book.controller;

import com.littlebook.book.dto.BookRequest;
import com.littlebook.book.dto.BookResponse;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    // --- Diagnostics ---

    @GetMapping("/health")
    public String health() { return "book-service OK"; }

    @GetMapping("/ping")
    public String ping() { return "pong"; }

    // --- CRUD ---

    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody BookRequest req) {
        BookEntity saved = service.create(mapToEntity(req));
        return ResponseEntity
                .created(URI.create("/books/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(mapToResponse(service.getById(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable UUID id, @RequestBody BookRequest req) {
        try {
            BookEntity updated = service.update(id, mapToEntity(req));
            return ResponseEntity.ok(mapToResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
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

    @GetMapping("/by-isbn13")
    public ResponseEntity<BookResponse> getByIsbn13(@RequestParam String isbn13) {
        return service.findByIsbn13(isbn13)
                .map(b -> ResponseEntity.ok(mapToResponse(b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-olid")
    public ResponseEntity<BookResponse> getByOpenlibraryId(@RequestParam String olId) {
        return service.findByOpenlibraryId(olId)
                .map(b -> ResponseEntity.ok(mapToResponse(b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
