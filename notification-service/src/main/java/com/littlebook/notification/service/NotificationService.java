package com.littlebook.notification.service;

import com.littlebook.notification.entity.NotificationEntity;
import com.littlebook.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public String ping() {
        return "pong";
    }

    public NotificationEntity create(NotificationEntity n) {
        if (n.getUserUuid() == null || n.getUserUuid().isBlank()) {
            throw new IllegalArgumentException("userUuid is required");
        }
        if (n.getType() == null || n.getType().isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        if (n.getTitle() == null || n.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (n.getMessage() == null || n.getMessage().isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        n.setId(null); // jamais trust l'id client
        return repo.save(n);
    }

    public NotificationEntity getById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    public Page<NotificationEntity> listByUser(String userUuid, boolean unreadOnly, Pageable pageable) {
        return unreadOnly
                ? repo.findByUserUuidAndReadFlagFalse(userUuid, pageable)
                : repo.findByUserUuid(userUuid, pageable);
    }

    public NotificationEntity markRead(UUID id) {
        NotificationEntity n = getById(id);
        n.setReadFlag(true);
        return repo.save(n);
    }

    public long unreadCount(String userUuid) {
        return repo.countByUserUuidAndReadFlagFalse(userUuid);
    }

    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
