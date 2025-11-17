package com.littlebook.book.repository;

import com.littlebook.book.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<BookEntity, UUID> {
    Optional<BookEntity> findByIsbn13(String isbn13);
    Optional<BookEntity> findByOpenlibraryId(String openlibraryId);
}
