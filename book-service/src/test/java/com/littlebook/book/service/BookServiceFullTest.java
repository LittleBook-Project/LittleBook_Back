package com.littlebook.book.service;

import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.exception.BookNotFoundException;
import com.littlebook.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceFullTest {

    @Mock
    private BookRepository repo;

    @InjectMocks
    private BookService service;

    @Captor
    private ArgumentCaptor<BookEntity> bookCaptor;

    private BookEntity sampleBook(UUID id) {
        BookEntity b = new BookEntity();
        b.setId(id);
        b.setTitle("Title " + id.toString().substring(0, 4));
        b.setSubtitle("Subtitle");
        b.setAuthors("Author A;Author B");
        b.setIsbn13("9781234567897");
        b.setOpenlibraryId("OL123M");
        b.setPublishYear(2020);
        b.setDescription("Desc");
        b.setCoverUrl("http://cover");
        b.setSubjects("subject1,subject2");
        b.setSourceData("{}");
        return b;
    }

    @Test
    @DisplayName("findAll should return all books from repository")
    void findAll_returnsAll() {
        List<BookEntity> list = Arrays.asList(sampleBook(UUID.randomUUID()), sampleBook(UUID.randomUUID()));
        when(repo.findAll()).thenReturn(list);

        List<BookEntity> res = service.findAll();

        assertNotNull(res);
        assertEquals(2, res.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    @DisplayName("list should return a paged result")
    void list_returnsPaged() {
        List<BookEntity> list = Arrays.asList(sampleBook(UUID.randomUUID()));
        Page<BookEntity> page = new PageImpl<>(list);
        Pageable p = PageRequest.of(0, 10);
    when(repo.findAll((org.springframework.data.jpa.domain.Specification<com.littlebook.book.entity.BookEntity>) any(), any(Pageable.class))).thenReturn(page);

    Page<BookEntity> res = service.list(null, null, null, null, null, null, null, p);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
    verify(repo).findAll((org.springframework.data.jpa.domain.Specification<com.littlebook.book.entity.BookEntity>) any(), eq(p));
    }

    @Test
    @DisplayName("findByIsbn13 should delegate to repository")
    void findByIsbn13_delegates() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.findByIsbn13("9781234567897")).thenReturn(Optional.of(b));

        Optional<BookEntity> res = service.findByIsbn13("9781234567897");

        assertTrue(res.isPresent());
        assertEquals(b.getTitle(), res.get().getTitle());
        verify(repo).findByIsbn13("9781234567897");
    }

    @Test
    @DisplayName("findByOpenlibraryId should delegate to repository")
    void findByOpenlibraryId_delegates() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.findByOpenlibraryId("OL123M")).thenReturn(Optional.of(b));

        Optional<BookEntity> res = service.findByOpenlibraryId("OL123M");

        assertTrue(res.isPresent());
        assertEquals(b.getOpenlibraryId(), res.get().getOpenlibraryId());
        verify(repo).findByOpenlibraryId("OL123M");
    }

    @Test
    @DisplayName("getById should return when found and throw when not found")
    void getById_foundAndNotFound() {
        UUID id = UUID.randomUUID();
        BookEntity b = sampleBook(id);
        when(repo.findById(id)).thenReturn(Optional.of(b));

        BookEntity ok = service.getById(id);
        assertEquals(b.getTitle(), ok.getTitle());

        UUID missing = UUID.randomUUID();
        when(repo.findById(missing)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> service.getById(missing));
    }

    @Test
    @DisplayName("create should save entity")
    void create_saves() {
        BookEntity b = sampleBook(UUID.randomUUID());
        when(repo.save(any(BookEntity.class))).thenReturn(b);

        BookEntity res = service.create(b);

        assertNotNull(res);
        assertEquals(b.getTitle(), res.getTitle());
        verify(repo).save(b);
    }

    @Test
    @DisplayName("update should apply fields and save")
    void update_appliesAndSaves() {
        UUID id = UUID.randomUUID();
        BookEntity existing = sampleBook(id);
        existing.setTitle("Old title");
        BookEntity newBook = sampleBook(id);
        newBook.setTitle("New title");
        newBook.setSubtitle("New subtitle");

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookEntity updated = service.update(id, newBook);

        assertEquals("New title", updated.getTitle());
        assertEquals("New subtitle", updated.getSubtitle());
        verify(repo).findById(id);
        verify(repo).save(bookCaptor.capture());
        BookEntity saved = bookCaptor.getValue();
        assertEquals("New title", saved.getTitle());
    }

    @Test
    @DisplayName("delete should call repository deleteById")
    void delete_callsRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(repo).deleteById(id);

        service.delete(id);

        verify(repo).deleteById(id);
    }
}
