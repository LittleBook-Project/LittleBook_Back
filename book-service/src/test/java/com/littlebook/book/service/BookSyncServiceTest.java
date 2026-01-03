package com.littlebook.book.service;

import com.littlebook.book.dto.openlibrary.OpenLibraryBook;
import com.littlebook.book.dto.openlibrary.OpenLibrarySearchResponse;
import com.littlebook.book.entity.BookEntity;
import com.littlebook.book.service.external.OpenLibraryClient;
import org.springframework.web.client.RestClient;
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
    private com.littlebook.book.repository.BookRepository repo;

    private BookService bookService;

    private OpenLibraryClient openLibraryClient;

    private BookSyncService bookSyncService;

    @Captor
    private ArgumentCaptor<BookEntity> bookEntityCaptor;

    @org.junit.jupiter.api.BeforeEach
    void init() {
        // instantiate a real BookService backed by a mocked repository to avoid mocking the concrete class
        this.bookService = new BookService(repo);
        // bookSyncService will be created per-test after preparing openLibraryClient stub
    }

    @Test
    void syncByIsbn_existingBook_updatesLastSyncedAtAndCallsUpdate() {
        String isbn = "9781234567897";

        UUID existingId = UUID.randomUUID();
        BookEntity existing = new BookEntity();
        existing.setId(existingId);
        existing.setIsbn13(isbn);
        existing.setLastSyncedAt(LocalDateTime.now().minusDays(1));


    when(repo.findByIsbn13(isbn)).thenReturn(Optional.of(existing));
    when(repo.findById(existingId)).thenReturn(Optional.of(existing));
    // prepare a no-op OpenLibraryClient stub
    RestClient.Builder builderMock = mock(RestClient.Builder.class);
    RestClient restMock = mock(RestClient.class);
    when(builderMock.build()).thenReturn(restMock);
    this.openLibraryClient = new OpenLibraryClient(builderMock, "http://openlib", 1000) {};
    this.bookSyncService = new BookSyncService(bookService, openLibraryClient);
    BookEntity updatedReturn = new BookEntity();
    updatedReturn.setId(existingId);
    when(repo.save(any(BookEntity.class))).thenReturn(updatedReturn);

    BookEntity result = bookSyncService.syncByIsbn(isbn);

        assertNotNull(result);

    // verify that repo.save was called via BookService.update
    verify(repo).save(bookEntityCaptor.capture());
        BookEntity passed = bookEntityCaptor.getValue();

        assertNotNull(passed.getLastSyncedAt());
    }

    @Test
    void syncByIsbn_notFoundOnServiceAndOpenLibrary_returnsNull() {
        String isbn = "0000000000";

    when(repo.findByIsbn13(isbn)).thenReturn(Optional.empty());
    when(repo.findByIsbn13(isbn.replace("-", ""))).thenReturn(Optional.empty());
    // stub OpenLibraryClient to record invocation and return null
    java.util.concurrent.atomic.AtomicBoolean called = new java.util.concurrent.atomic.AtomicBoolean(false);
    RestClient.Builder builderMock2 = mock(RestClient.Builder.class);
    RestClient restMock2 = mock(RestClient.class);
    when(builderMock2.build()).thenReturn(restMock2);
    this.openLibraryClient = new OpenLibraryClient(builderMock2, "http://openlib", 1000) {
        @Override
        public OpenLibrarySearchResponse searchByIsbn(String i) {
            called.set(true);
            return null;
        }
    };
    this.bookSyncService = new BookSyncService(bookService, openLibraryClient);

    BookEntity result = bookSyncService.syncByIsbn(isbn);

    assertNull(result);
    assertTrue(called.get(), "Expected searchByIsbn to be invoked");
    verify(repo, never()).save(any());
    }

    @Test
    void syncByIsbn_foundOnOpenLibrary_createsBookAndMapsFields() {
        String isbn = "9781111111111";

    when(repo.findByIsbn13(isbn)).thenReturn(Optional.empty());
    when(repo.findByIsbn13(isbn.replace("-", ""))).thenReturn(Optional.empty());

    OpenLibraryBook olBook = new OpenLibraryBook();
        olBook.setKey("/works/OLID-1");
        olBook.setTitle("Test Title");
        olBook.setAuthorNames(Arrays.asList("Author One"));
        olBook.setIsbn13(Arrays.asList(isbn));
        olBook.setIsbn(Arrays.asList("0123456789"));
        olBook.setSubjects(Arrays.asList("Subj"));
        olBook.setCoverI(123);
        olBook.setDescription("desc");

    OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
    response.setDocs(Arrays.asList(olBook));
    // create OpenLibraryClient stub that returns our response and records call
    java.util.concurrent.atomic.AtomicBoolean calledCreate = new java.util.concurrent.atomic.AtomicBoolean(false);
    RestClient.Builder builderMock3 = mock(RestClient.Builder.class);
    RestClient restMock3 = mock(RestClient.class);
    when(builderMock3.build()).thenReturn(restMock3);
    this.openLibraryClient = new OpenLibraryClient(builderMock3, "http://openlib", 1000) {
        @Override
        public OpenLibrarySearchResponse searchByIsbn(String i) {
            calledCreate.set(true);
            return response;
        }
    };
    this.bookSyncService = new BookSyncService(bookService, openLibraryClient);

    UUID createdId = UUID.randomUUID();
    BookEntity created = new BookEntity();
    created.setId(createdId);
    when(repo.save(any(BookEntity.class))).thenReturn(created);

    BookEntity result = bookSyncService.syncByIsbn(isbn);

    assertNotNull(result);
    assertEquals(createdId, result.getId());

    verify(repo).save(bookEntityCaptor.capture());
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

    // create stub OpenLibraryClient returning null for search
    RestClient.Builder builderMock4 = mock(RestClient.Builder.class);
    RestClient restMock4 = mock(RestClient.class);
    when(builderMock4.build()).thenReturn(restMock4);
    this.openLibraryClient = new OpenLibraryClient(builderMock4, "http://openlib", 1000) {
        @Override
        public OpenLibrarySearchResponse search(String title, String author, String isbn, int limit) {
            return null;
        }
    };
    this.bookSyncService = new BookSyncService(bookService, openLibraryClient);

    Page<BookEntity> page = bookSyncService.searchAndSync("title", "author", pageable);

    assertNotNull(page);
    assertEquals(0, page.getTotalElements());
    assertTrue(page.getContent().isEmpty());

    verify(repo, never()).save(any());
    verify(repo, never()).findByIsbn13(anyString());
    }

    @Test
    void searchAndSync_existingByIsbn_returnsExistingWithoutCreating() {
        PageRequest pageable = PageRequest.of(0, 10);

    OpenLibraryBook olBook = new OpenLibraryBook();
    olBook.setIsbn13(Arrays.asList("9782222222222"));

    OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
    response.setDocs(Arrays.asList(olBook));
        response.setNumFound(1);


    // create stub OpenLibraryClient returning response for search
    RestClient.Builder builderMock5 = mock(RestClient.Builder.class);
    RestClient restMock5 = mock(RestClient.class);
    when(builderMock5.build()).thenReturn(restMock5);
    this.openLibraryClient = new OpenLibraryClient(builderMock5, "http://openlib", 1000) {
        @Override
        public OpenLibrarySearchResponse search(String title, String author, String isbn, int limit) {
            return response;
        }
    };
    this.bookSyncService = new BookSyncService(bookService, openLibraryClient);

    UUID existingId = UUID.randomUUID();
    BookEntity existing = new BookEntity();
    existing.setId(existingId);

    when(repo.findByIsbn13("9782222222222")).thenReturn(Optional.of(existing));

    Page<BookEntity> page = bookSyncService.searchAndSync(null, "someAuthor", pageable);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertSame(existing, page.getContent().get(0));

        verify(repo, never()).save(any());
    }

}