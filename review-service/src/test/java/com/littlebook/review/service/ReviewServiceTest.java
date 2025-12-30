package com.littlebook.review.service;

import com.littlebook.review.entity.ReviewEntity;
import com.littlebook.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository repo;

    @InjectMocks
    private ReviewService service;

    @Captor
    private ArgumentCaptor<ReviewEntity> entityCaptor;

    private ReviewEntity sample;

    @BeforeEach
    void setUp() {
        sample = new ReviewEntity();
        sample.setId(1L);
        sample.setBookIsbn("978-1-2345-6789-7");
        sample.setBookId("OL12345M");
        sample.setUserUuid("user-uuid");
        sample.setDescription("Nice book");
        sample.setRating(4);
        sample.setReviewCreationDate(LocalDate.of(2025, 1, 1));
    }

    @Test
    void ping_returnsPong() {
        assertEquals("pong", service.ping());
    }

    @Test
    void findAll_delegatesToRepo() {
        when(repo.findAll()).thenReturn(List.of(sample));
        var all = service.findAll();
        assertNotNull(all);
        assertEquals(1, all.size());
        verify(repo).findAll();
    }

    @Test
    void getById_foundAndNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample));
        var opt = service.getById(1L);
        assertTrue(opt.isPresent());
        assertEquals(sample.getBookIsbn(), opt.get().getBookIsbn());

        when(repo.findById(2L)).thenReturn(Optional.empty());
        var opt2 = service.getById(2L);
        assertTrue(opt2.isEmpty());
    }

    @Test
    void finders_delegateToRepo() {
        when(repo.findByBookIsbn("978-1-2345-6789-7")).thenReturn(List.of(sample));
        when(repo.findByBookId("OL12345M")).thenReturn(List.of(sample));
        when(repo.findByUserUuid("user-uuid")).thenReturn(List.of(sample));

        assertEquals(1, service.getByBookIsbn("978-1-2345-6789-7").size());
        assertEquals(1, service.getByBookId("OL12345M").size());
        assertEquals(1, service.getByUserUuid("user-uuid").size());

        verify(repo).findByBookIsbn("978-1-2345-6789-7");
        verify(repo).findByBookId("OL12345M");
        verify(repo).findByUserUuid("user-uuid");
    }

    @Test
    void averages_and_counts_delegation() {
        when(repo.getAverageRatingByBookIsbn("978-1-2345-6789-7")).thenReturn(4.25);
        when(repo.getAverageRatingByBookId("OL12345M")).thenReturn(3.5);
        when(repo.countByBookIsbn("978-1-2345-6789-7")).thenReturn(5L);
        when(repo.countByBookId("OL12345M")).thenReturn(2L);

        assertEquals(4.25, service.getAverageRating("978-1-2345-6789-7"));
        assertEquals(3.5, service.getAverageRatingByBookId("OL12345M"));
        assertEquals(5L, service.getReviewCount("978-1-2345-6789-7"));
        assertEquals(2L, service.getReviewCountByBookId("OL12345M"));
    }

    @Test
    void create_validAndInvalid() {
        ReviewEntity toCreate = new ReviewEntity();
        toCreate.setBookIsbn("978-1-2345-6789-7");
        toCreate.setUserUuid("u1");
        toCreate.setRating(5);
        toCreate.setDescription("Great");

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.create(toCreate);
        assertNotNull(saved.getReviewCreationDate());
        verify(repo).save(entityCaptor.capture());
        assertEquals(5, entityCaptor.getValue().getRating());

        ReviewEntity bad = new ReviewEntity();
        bad.setRating(0);
        assertThrows(IllegalArgumentException.class, () -> service.create(bad));

        ReviewEntity bad2 = new ReviewEntity();
        bad2.setRating(6);
        assertThrows(IllegalArgumentException.class, () -> service.create(bad2));
    }

    @Test
    void create_boundaryRatings_and_extremes() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewEntity low = new ReviewEntity();
        low.setBookIsbn("978-1-2345-6789-7");
        low.setUserUuid("u1");
        low.setRating(1);
        var savedLow = service.create(low);
        assertNotNull(savedLow.getReviewCreationDate());
        verify(repo, atLeastOnce()).save(any());

        ReviewEntity high = new ReviewEntity();
        high.setBookIsbn("978-1-2345-6789-7");
        high.setUserUuid("u2");
        high.setRating(5);
        var savedHigh = service.create(high);
        assertEquals(5, savedHigh.getRating());

        // extreme invalid values
        ReviewEntity neg = new ReviewEntity();
        neg.setRating(-1);
        assertThrows(IllegalArgumentException.class, () -> service.create(neg));

        ReviewEntity huge = new ReviewEntity();
        huge.setRating(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> service.create(huge));
    }

    @Test
    void create_nullFields_throws() {
    ReviewEntity r = new ReviewEntity();
        r.setRating(3);
        r.setBookIsbn(null);
        r.setUserUuid(null);
        assertThrows(IllegalArgumentException.class, () -> service.create(r));
    }

    @Test
    void update_success_and_errors() {
        ReviewEntity existing = new ReviewEntity();
        existing.setId(1L);
        existing.setDescription("old");
        existing.setRating(3);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewEntity newVals = new ReviewEntity();
        newVals.setDescription("new desc");
        newVals.setRating(5);

        var updated = service.update(1L, newVals);
        assertEquals("new desc", updated.getDescription());
        assertEquals(5, updated.getRating());

        // invalid rating
        ReviewEntity newBad = new ReviewEntity();
        newBad.setRating(10);
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, newBad));

        // not found
        when(repo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(2L, newVals));
    }

    @Test
    void update_invalidRating_throws() {
        ReviewEntity existing = new ReviewEntity();
        existing.setId(1L);
        existing.setDescription("old");
        existing.setRating(3);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        ReviewEntity bad = new ReviewEntity();
        bad.setRating(1000);
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, bad));
    }

    @Test
    void delete_success_and_errors() {
        ReviewEntity existing = new ReviewEntity();
        existing.setId(1L);
        existing.setUserUuid("owner-uuid");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        // correct user -> delete
        service.delete(1L, "owner-uuid");
        verify(repo).delete(existing);

        // not found
        when(repo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.delete(2L, "x"));

        // wrong user
        ReviewEntity other = new ReviewEntity();
        other.setId(3L);
        other.setUserUuid("someone-else");
        when(repo.findById(3L)).thenReturn(Optional.of(other));
        assertThrows(IllegalArgumentException.class, () -> service.delete(3L, "not-owner"));

        // null userUuid should be rejected
        when(repo.findById(4L)).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class, () -> service.delete(4L, null));
    }
}
