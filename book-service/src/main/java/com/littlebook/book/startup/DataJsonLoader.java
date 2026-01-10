package com.littlebook.book.startup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlebook.book.dto.BookRequest;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Component
public class DataJsonLoader {

    private static final Logger log = LoggerFactory.getLogger(DataJsonLoader.class);

    private final BookService bookService;
    private final ObjectMapper mapper;

    @Value("${app.data.books-file:/data/books.json}")
    private String booksFilePath;

    public DataJsonLoader(BookService bookService, ObjectMapper mapper) {
        this.bookService = bookService;
        this.mapper = mapper;
    }

    @PostConstruct
    public void load() {
        try {
            File f = new File(booksFilePath);
            if (!f.exists()) {
                log.info("No books file found at {} — skipping import", booksFilePath);
                return;
            }
            log.info("Loading books from {}", booksFilePath);
            byte[] bytes = Files.readAllBytes(f.toPath());
            List<BookRequest> list = mapper.readValue(bytes, new TypeReference<>() {});
            int added = 0;
            for (BookRequest r : list) {
                try {
                    // Check by openlibraryId
                    Optional<com.littlebook.book.entity.BookEntity> existing = Optional.empty();
                    if (r.getOpenlibraryId() != null && !r.getOpenlibraryId().isBlank()) {
                        existing = bookService.findByOpenlibraryId(r.getOpenlibraryId());
                    }
                    if (existing.isEmpty() && r.getIsbn13() != null && !r.getIsbn13().isBlank()) {
                        existing = bookService.findByIsbn13(r.getIsbn13());
                    }

                    BookEntity e = new BookEntity();
                    e.setOpenlibraryId(r.getOpenlibraryId());
                    e.setIsbn10(r.getIsbn10());
                    e.setIsbn13(r.getIsbn13());
                    e.setTitle(r.getTitle());
                    e.setSubtitle(r.getSubtitle());
                    e.setAuthors(r.getAuthors());
                    e.setPublishYear(r.getPublishYear());
                    e.setCoverUrl(r.getCoverUrl());
                    e.setDescription(r.getDescription());
                    e.setSubjects(r.getSubjects());
                    e.setSourceData(r.getSourceData());

                    if (existing.isPresent()) {
                        // update existing
                        bookService.update(existing.get().getId(), e);
                    } else {
                        bookService.create(e);
                        added++;
                    }
                } catch (Exception ex) {
                    log.warn("Failed to import book {}: {}", r.getTitle(), ex.getMessage());
                }
            }
            log.info("Imported {} books from {}", added, booksFilePath);
        } catch (Exception ex) {
            log.error("Failed to load books from {}: {}", booksFilePath, ex.getMessage(), ex);
        }
    }
}
