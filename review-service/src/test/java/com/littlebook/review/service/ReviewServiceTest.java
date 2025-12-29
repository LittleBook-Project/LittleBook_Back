package com.littlebook.review.service;

import com.littlebook.review.entity.ReviewEntity;
import com.littlebook.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ReviewService svc;

    private ReviewEntity r;

    @BeforeEach
    void setUp() {
        r = new ReviewEntity();
        r.setId(1L);
        r.setBookIsbn("isbn-1");
        r.setUserUuid("user-1");
        r.setRating(4);
        r.setDescription("ok");
        r.setReviewCreationDate(LocalDate.now());
    }

    @Test
    void ping_returnsPong() {
        assertEquals("pong", svc.ping());
    }

    @Test
    void create_valid_setsDateAndSaves() {
        ReviewEntity toCreate = new ReviewEntity();
        toCreate.setRating(5);
        toCreate.setBookIsbn("isbn-1");
        toCreate.setUserUuid("u1");

        when(repo.save(any())).thenAnswer(inv -> {
            ReviewEntity rr = inv.getArgument(0);
            rr.setId(2L);
            return rr;
        });

        ReviewEntity out = svc.create(toCreate);

        assertNotNull(out.getId());
        assertNotNull(out.getReviewCreationDate());
        verify(repo).save(any());
    }

    @Test
    void create_invalidRating_throws() {
        ReviewEntity bad = new ReviewEntity(); bad.setRating(0);
        assertThrows(IllegalArgumentException.class, () -> svc.create(bad));
    }

    @Test
    void update_updatesAllowedFields() {
        ReviewEntity updated = new ReviewEntity(); updated.setDescription("new"); updated.setRating(3);
        when(repo.findById(1L)).thenReturn(Optional.of(r));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewEntity out = svc.update(1L, updated);

        assertEquals("new", out.getDescription());
        assertEquals(3, out.getRating());
        verify(repo).save(r);
    }

    @Test
    void update_invalidRating_throws() {
        ReviewEntity updated = new ReviewEntity(); updated.setRating(10);
        when(repo.findById(1L)).thenReturn(Optional.of(r));
        assertThrows(IllegalArgumentException.class, () -> svc.update(1L, updated));
    }

    @Test
    void delete_onlyOwner_canDelete() {
        when(repo.findById(1L)).thenReturn(Optional.of(r));
        doNothing().when(repo).delete(r);

        svc.delete(1L, "user-1");

        verify(repo).delete(r);
    }

    @Test
    void delete_wrongUser_throws() {
        when(repo.findById(1L)).thenReturn(Optional.of(r));
        assertThrows(IllegalArgumentException.class, () -> svc.delete(1L, "other"));
    }

    @Test
    void getters_delegateToRepo() {
        when(repo.findAll()).thenReturn(List.of(r));
        when(repo.findByBookIsbn("isbn-1")).thenReturn(List.of(r));
        when(repo.findByUserUuid("user-1")).thenReturn(List.of(r));
        when(repo.getAverageRatingByBookIsbn("isbn-1")).thenReturn(4.0);
        when(repo.countByBookIsbn("isbn-1")).thenReturn(1L);

        assertEquals(1, svc.findAll().size());
        assertEquals(1, svc.getByBookIsbn("isbn-1").size());
        assertEquals(1, svc.getByUserUuid("user-1").size());
        assertEquals(4.0, svc.getAverageRating("isbn-1"));
        assertEquals(1L, svc.getReviewCount("isbn-1"));
    }
}
