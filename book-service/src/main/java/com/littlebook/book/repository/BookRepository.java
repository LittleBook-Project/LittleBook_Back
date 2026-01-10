package com.littlebook.book.repository;

import com.littlebook.book.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<BookEntity, UUID>, JpaSpecificationExecutor<BookEntity> {
    Optional<BookEntity> findByIsbn13(String isbn13);
    Optional<BookEntity> findByOpenlibraryId(String openlibraryId);
    org.springframework.data.domain.Page<com.littlebook.book.entity.BookEntity> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(
            String title, String authors, org.springframework.data.domain.Pageable pageable);
}
