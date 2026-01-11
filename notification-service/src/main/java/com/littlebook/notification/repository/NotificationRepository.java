package com.littlebook.notification.repository;

import com.littlebook.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserUuid(String userUuid, Pageable pageable);

    Page<NotificationEntity> findByUserUuidAndReadFlagFalse(String userUuid, Pageable pageable);

    long countByUserUuidAndReadFlagFalse(String userUuid);
}
