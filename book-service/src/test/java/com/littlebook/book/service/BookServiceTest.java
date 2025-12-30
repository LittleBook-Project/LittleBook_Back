package com.littlebook.book.service;

import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.exception.BookNotFoundException;
import com.littlebook.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repo;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(repo);
    }

    private BookEntity sampleBook(UUID id) {
        BookEntity b = new BookEntity();
        b.setId(id);
        b.setTitle("Title");
        b.setSubtitle("Sub");
        // authors stored as comma-separated string in entity
        b.setAuthors("Author A");
        b.setPublishYear(2020);
        b.setDescription("Desc");
        b.setCoverUrl("http://cover");
        // subjects stored as comma-separated string
        b.setSubjects("Subject1");
        b.setIsbn13("9781234567897");
        b.setOpenlibraryId("OL123M");
        return b;
    }

    @Test
    void findAll_delegatesToRepository() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.findAll()).thenReturn(List.of(b));

        List<BookEntity> out = service.findAll();
        assertEquals(1, out.size());
        assertEquals(b, out.get(0));
        verify(repo).findAll();
    }

    @Test
    void list_returnsPagedResults() {
        BookEntity b1 = sampleBook(UUID.randomUUID());
        Pageable p = PageRequest.of(0, 10);
        Page<BookEntity> page = new PageImpl<>(List.of(b1));
        when(repo.findAll(p)).thenReturn(page);

        Page<BookEntity> res = service.list(null, null, null, p);
        assertEquals(1, res.getTotalElements());
        assertEquals(b1, res.getContent().get(0));
        verify(repo).findAll(p);
    }

    @Test
    void findByIsbn13_returnsOptional() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.findByIsbn13("9781234567897")).thenReturn(Optional.of(b));

        Optional<BookEntity> res = service.findByIsbn13("9781234567897");
        assertTrue(res.isPresent());
        assertEquals(b, res.get());
    }

    @Test
    void findByOpenlibraryId_returnsOptional() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.findByOpenlibraryId("OL123M")).thenReturn(Optional.of(b));

        Optional<BookEntity> res = service.findByOpenlibraryId("OL123M");
        assertTrue(res.isPresent());
        assertEquals(b, res.get());
    }

    @Test
    void getById_returnsEntity_orThrows() {
        UUID id = UUID.randomUUID();
        BookEntity b = sampleBook(id);
        when(repo.findById(id)).thenReturn(Optional.of(b));

        BookEntity out = service.getById(id);
        assertEquals(b, out);

        UUID missing = UUID.randomUUID();
        when(repo.findById(missing)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> service.getById(missing));
    }

    @Test
    void create_savesAndReturns() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.save(b)).thenReturn(b);

        BookEntity out = service.create(b);
        assertEquals(b, out);
        verify(repo).save(b);
    }

    @Test
    void update_updatesFields_orThrows() {
        UUID id = UUID.randomUUID();
        BookEntity existing = sampleBook(id);
    BookEntity updated = new BookEntity();
        updated.setTitle("New Title");
        updated.setSubtitle("New Sub");
    updated.setAuthors("Author B");
        updated.setPublishYear(2021);
        updated.setDescription("New Desc");
        updated.setCoverUrl("http://newcover");
    updated.setSubjects("S2");
    updated.setSourceData("{\"k\":\"v\"}");

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookEntity out = service.update(id, updated);
        assertEquals("New Title", out.getTitle());
        assertEquals("New Sub", out.getSubtitle());
    assertEquals("Author B", out.getAuthors());
        assertEquals(2021, out.getPublishYear());
        assertEquals("New Desc", out.getDescription());
        assertEquals("http://newcover", out.getCoverUrl());
    assertEquals("S2", out.getSubjects());
    assertEquals("{\"k\":\"v\"}", out.getSourceData());

        UUID missing = UUID.randomUUID();
        when(repo.findById(missing)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> service.update(missing, updated));
    }

    @Test
    void delete_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(repo).deleteById(id);
        service.delete(id);
        verify(repo).deleteById(id);
    }
}
