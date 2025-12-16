package com.littlebook.review.repository;

import com.littlebook.review.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    List<ReviewEntity> findByBookIsbn(String isbn);

    List<ReviewEntity> findByUserUuid(String userUuid);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookIsbn = :isbn")
    Double getAverageRatingByBookIsbn(String isbn);

    Long countByBookIsbn(String isbn);
}
