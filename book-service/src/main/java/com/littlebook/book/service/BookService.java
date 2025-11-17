package com.littlebook.book.service;

import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {
    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public List<BookEntity> findAll() {
        return repo.findAll();
    }

    public Page<BookEntity> list(String q, String isbn, String openlibraryId, Pageable pageable) {
        // For now, ignore query filters and return paged results. Repository provides findAll(pageable).
        return repo.findAll(pageable);
    }

    public Optional<BookEntity> findByIsbn13(String isbn13) {
        return repo.findByIsbn13(isbn13);
    }

    public Optional<BookEntity> findByOpenlibraryId(String openlibraryId) {
        return repo.findByOpenlibraryId(openlibraryId);
    }

    public BookEntity getById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
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
