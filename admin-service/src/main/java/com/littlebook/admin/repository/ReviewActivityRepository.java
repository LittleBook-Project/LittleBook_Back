package com.littlebook.admin.repository;

import com.littlebook.admin.entity.ReviewActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewActivityRepository extends JpaRepository<ReviewActivity, Long> {
    long countByEventType(String eventType);
}
