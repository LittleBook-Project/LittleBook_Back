package com.littlebook.notification.controller;

import com.littlebook.notification.dto.NotificationRequest;
import com.littlebook.notification.dto.NotificationResponse;
import com.littlebook.notification.entity.NotificationEntity;
import com.littlebook.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public String health() { return "notification-service OK"; }

    @GetMapping("/ping")
    public String ping() { return service.ping(); }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationRequest req) {
        NotificationEntity saved = service.create(mapToEntity(req));
        return ResponseEntity
                .created(URI.create("/notifications/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(mapToResponse(service.getById(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public Page<NotificationResponse> listByUser(@RequestParam String userUuid,
                                                 @RequestParam(defaultValue = "false") boolean unreadOnly,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return service.listByUser(userUuid, unreadOnly, pageable).map(this::mapToResponse);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@RequestParam String userUuid) {
        return ResponseEntity.ok(service.unreadCount(userUuid));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(mapToResponse(service.markRead(id)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- mapping

    private NotificationEntity mapToEntity(NotificationRequest r) {
        NotificationEntity e = new NotificationEntity();
        e.setUserUuid(r.getUserUuid());
        e.setType(r.getType());
        e.setTitle(r.getTitle());
        e.setMessage(r.getMessage());
        return e;
    }

    private NotificationResponse mapToResponse(NotificationEntity e) {
        return new NotificationResponse(
                e.getId(),
                e.getUserUuid(),
                e.getType(),
                e.getTitle(),
                e.getMessage(),
                e.isReadFlag(),
                e.getCreatedAt()
        );
    }
}
