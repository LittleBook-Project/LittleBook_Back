package com.littlebook.book.service;

import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.exception.BookNotFoundException;
import com.littlebook.book.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public List<BookEntity> findAll() {
        return repo.findAll();
    }

    public Page<BookEntity> list(String q, String isbn, String openlibraryId, String author, String subjects,
                                 Integer minYear, Integer maxYear, Pageable pageable) {
        // Dynamic filtering: supports q (title/authors), isbn, openlibraryId, subjects, year range
        // To keep API stable we build a JPA Specification dynamically
        org.springframework.data.jpa.domain.Specification<BookEntity> spec = (root, query, cb) -> cb.conjunction();

        if (q != null && !q.isBlank()) {
            String p = "%" + q.toLowerCase() + "%";
            org.springframework.data.jpa.domain.Specification<BookEntity> titleSpec = (root, query1, cb1) -> cb1.like(cb1.lower(root.get("title")), p);
            org.springframework.data.jpa.domain.Specification<BookEntity> authorsSpec = (root, query1, cb1) -> cb1.like(cb1.lower(root.get("authors")), p);
            spec = spec.and(titleSpec.or(authorsSpec));
        }

        if (isbn != null && !isbn.isBlank()) {
            org.springframework.data.jpa.domain.Specification<BookEntity> isbnSpec = (root, query1, cb1) -> cb1.equal(root.get("isbn13"), isbn);
            spec = spec.and(isbnSpec);
        }

        if (openlibraryId != null && !openlibraryId.isBlank()) {
            org.springframework.data.jpa.domain.Specification<BookEntity> olSpec = (root, query1, cb1) -> cb1.equal(root.get("openlibraryId"), openlibraryId);
            spec = spec.and(olSpec);
        }

        if (author != null && !author.isBlank()) {
            String p = "%" + author.toLowerCase() + "%";
            org.springframework.data.jpa.domain.Specification<BookEntity> authorSpec = (root, query1, cb1) -> cb1.like(cb1.lower(root.get("authors")), p);
            spec = spec.and(authorSpec);
        }

        if (subjects != null && !subjects.isBlank()) {
            String p = "%" + subjects.toLowerCase() + "%";
            org.springframework.data.jpa.domain.Specification<BookEntity> subjSpec = (root, query1, cb1) -> cb1.like(cb1.lower(root.get("subjects")), p);
            spec = spec.and(subjSpec);
        }

        if (minYear != null) {
            org.springframework.data.jpa.domain.Specification<BookEntity> minY = (root, query1, cb1) -> cb1.greaterThanOrEqualTo(root.get("publishYear"), minYear);
            spec = spec.and(minY);
        }

        if (maxYear != null) {
            org.springframework.data.jpa.domain.Specification<BookEntity> maxY = (root, query1, cb1) -> cb1.lessThanOrEqualTo(root.get("publishYear"), maxYear);
            spec = spec.and(maxY);
        }

        // If pageable sort is provided it'll be applied by the repository call
        return repo.findAll(spec, pageable);
    }

    /**
     * Local search helper used by controller to detect whether local DB has matches.
     */
    public Page<BookEntity> searchLocal(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return new org.springframework.data.domain.PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
        return repo.findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(q, q, pageable);
    }

    public Optional<BookEntity> findByIsbn13(String isbn13) {
        return repo.findByIsbn13(isbn13);
    }

    public Optional<BookEntity> findByOpenlibraryId(String openlibraryId) {
        return repo.findByOpenlibraryId(openlibraryId);
    }

    public BookEntity getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));
    }

    public BookEntity create(BookEntity b) {
        return repo.save(b);
    }

    public BookEntity update(UUID id, BookEntity newBook) {
        BookEntity b = getById(id);
        b.setTitle(newBook.getTitle());
        b.setSubtitle(newBook.getSubtitle());
        b.setAuthors(newBook.getAuthors());
        b.setPublishYear(newBook.getPublishYear());
        b.setDescription(newBook.getDescription());
        b.setCoverUrl(newBook.getCoverUrl());
        b.setSubjects(newBook.getSubjects());
        b.setSourceData(newBook.getSourceData());
        return repo.save(b);
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
