package com.littlebook.book.service;

import com.littlebook.book.dto.openlibrary.OpenLibraryBook;
import com.littlebook.book.dto.openlibrary.OpenLibrarySearchResponse;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.service.external.OpenLibraryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSyncServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private OpenLibraryClient openLibraryClient;

    @InjectMocks
    private BookSyncService bookSyncService;

    @Captor
    private ArgumentCaptor<BookEntity> bookEntityCaptor;

    @BeforeEach
    void setup() {
        // nothing for now
    }

    @Test
    void syncByIsbn_existingBook_updatesLastSyncedAtAndCallsUpdate() {
        String isbn = "9781234567897";

        UUID existingId = UUID.randomUUID();
        BookEntity existing = new BookEntity();
        existing.setId(existingId);
        existing.setIsbn13(isbn);
        existing.setLastSyncedAt(LocalDateTime.now().minusDays(1));

        when(bookService.findByIsbn13(isbn)).thenReturn(Optional.of(existing));

        BookEntity updatedReturn = new BookEntity();
        updatedReturn.setId(existingId);
        when(bookService.update(eq(existingId), any(BookEntity.class))).thenReturn(updatedReturn);

        BookEntity result = bookSyncService.syncByIsbn(isbn);

        assertNotNull(result);

        verify(bookService).update(eq(existingId), bookEntityCaptor.capture());
        BookEntity passed = bookEntityCaptor.getValue();

        assertNotNull(passed.getLastSyncedAt());
    }

    @Test
    void syncByIsbn_notFoundOnServiceAndOpenLibrary_returnsNull() {
        String isbn = "0000000000";

        when(bookService.findByIsbn13(isbn)).thenReturn(Optional.empty());
        when(bookService.findByIsbn13(isbn.replace("-", ""))).thenReturn(Optional.empty());
        when(openLibraryClient.searchByIsbn(isbn)).thenReturn(null);

        BookEntity result = bookSyncService.syncByIsbn(isbn);

        assertNull(result);
        verify(openLibraryClient).searchByIsbn(isbn);
        verify(bookService, never()).create(any());
        verify(bookService, never()).update(any(UUID.class), any());
    }

    @Test
    void syncByIsbn_foundOnOpenLibrary_createsBookAndMapsFields() {
        String isbn = "9781111111111";

        when(bookService.findByIsbn13(isbn)).thenReturn(Optional.empty());
        when(bookService.findByIsbn13(isbn.replace("-", ""))).thenReturn(Optional.empty());

        OpenLibraryBook olBook = mock(OpenLibraryBook.class);
        when(olBook.getOpenLibraryId()).thenReturn("OLID-1");
        when(olBook.getTitle()).thenReturn("Test Title");
        when(olBook.getAuthorsAsString()).thenReturn("Author One");
        when(olBook.getIsbn13()).thenReturn(Arrays.asList(isbn));
        when(olBook.getIsbn()).thenReturn(Arrays.asList("0123456789"));

        OpenLibrarySearchResponse response = mock(OpenLibrarySearchResponse.class);
        when(response.getDocs()).thenReturn(Arrays.asList(olBook));
        when(openLibraryClient.searchByIsbn(isbn)).thenReturn(response);

        UUID createdId = UUID.randomUUID();
        BookEntity created = new BookEntity();
        created.setId(createdId);
        when(bookService.create(any(BookEntity.class))).thenReturn(created);

        BookEntity result = bookSyncService.syncByIsbn(isbn);

        assertNotNull(result);
        assertEquals(createdId, result.getId());

        verify(bookService).create(bookEntityCaptor.capture());
        BookEntity passed = bookEntityCaptor.getValue();

        assertEquals("OLID-1", passed.getOpenlibraryId());
        assertEquals("Test Title", passed.getTitle());
        assertEquals("Author One", passed.getAuthors());
        assertEquals(isbn, passed.getIsbn13());
        assertEquals("0123456789", passed.getIsbn10());

        assertNotNull(passed.getSourceData());
        assertTrue(passed.getSourceData().contains("Test Title"));
        assertTrue(passed.getSourceData().contains("Author One"));
    }

    @Test
    void searchAndSync_noResults_returnsEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 5);

        when(openLibraryClient.search("title", "author", null, pageable.getPageSize())).thenReturn(null);

        Page<BookEntity> page = bookSyncService.searchAndSync("title", "author", pageable);

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());

        verify(bookService, never()).create(any());
        verify(bookService, never()).findByIsbn13(anyString());
    }

    @Test
void searchAndSync_existingByIsbn_returnsExistingWithoutCreating() {
    PageRequest pageable = PageRequest.of(0, 10);

    OpenLibraryBook olBook = mock(OpenLibraryBook.class);
    when(olBook.getIsbn13()).thenReturn(Arrays.asList("9782222222222"));

    OpenLibrarySearchResponse response = mock(OpenLibrarySearchResponse.class);
    when(response.getDocs()).thenReturn(Arrays.asList(olBook));
    when(response.getNumFound()).thenReturn(1);

    when(openLibraryClient.search(null, "someAuthor", null, pageable.getPageSize())).thenReturn(response);

    UUID existingId = UUID.randomUUID();
    BookEntity existing = new BookEntity();
    existing.setId(existingId);

    when(bookService.findByIsbn13("9782222222222")).thenReturn(Optional.of(existing));

    Page<BookEntity> page = bookSyncService.searchAndSync(null, "someAuthor", pageable);

    assertNotNull(page);
    assertEquals(1, page.getTotalElements());
    assertEquals(1, page.getContent().size());
    assertSame(existing, page.getContent().get(0));

    verify(bookService, never()).create(any());
    }

}