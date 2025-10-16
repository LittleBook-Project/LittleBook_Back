package com.littlebook.controller;

import com.littlebook.entity.ReadingEntity;
import com.littlebook.dto.ReadingDTO;
import com.littlebook.enums.ReadingStatus;
import com.littlebook.service.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les listes de lecture des utilisateurs
 */
@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    /**
     * Ajoute un livre à la liste de lecture d'un utilisateur
     * POST /api/reading
     */
    @PostMapping
    public ResponseEntity<?> addBookToReading(@RequestBody AddBookToReadingRequest request) {
        try {
            ReadingEntity reading = readingService.addBookToReading(
                request.getUserUuid(), 
                request.getBookIsbn(), 
                request.getStatus()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(reading));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Met à jour le statut de lecture d'un livre
     * PUT /api/reading/{userUuid}/{bookIsbn}
     */
    @PutMapping("/{userUuid}/{bookIsbn}")
    public ResponseEntity<?> updateReadingStatus(
            @PathVariable UUID userUuid,
            @PathVariable String bookIsbn,
            @RequestBody UpdateReadingStatusRequest request) {
        try {
            ReadingEntity reading = readingService.updateReadingStatus(
                userUuid, 
                bookIsbn, 
                request.getStatus()
            );
            return ResponseEntity.ok(toDTO(reading));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprime un livre de la liste de lecture
     * DELETE /api/reading/{userUuid}/{bookIsbn}
     */
    @DeleteMapping("/{userUuid}/{bookIsbn}")
    public ResponseEntity<?> removeBookFromReading(
            @PathVariable UUID userUuid,
            @PathVariable String bookIsbn) {
        try {
            readingService.removeBookFromReading(userUuid, bookIsbn);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère toutes les lectures d'un utilisateur
     * GET /api/reading/user/{userUuid}
     */
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<List<ReadingEntity>> getUserReadings(@PathVariable UUID userUuid) {
        List<ReadingEntity> readings = readingService.getUserReadings(userUuid);
        return ResponseEntity.ok(readings);
    }

    /**
     * Récupère les lectures d'un utilisateur par statut
     * GET /api/reading/user/{userUuid}/status/{status}
     */
    @GetMapping("/user/{userUuid}/status/{status}")
    public ResponseEntity<List<ReadingEntity>> getUserReadingsByStatus(
            @PathVariable UUID userUuid,
            @PathVariable ReadingStatus status) {
        List<ReadingEntity> readings = readingService.getUserReadingsByStatus(userUuid, status);
        return ResponseEntity.ok(readings);
    }

    /**
     * Récupère les livres "À lire" d'un utilisateur
     * GET /api/reading/user/{userUuid}/to-read
     */
    @GetMapping("/user/{userUuid}/to-read")
    public ResponseEntity<List<ReadingEntity>> getBooksToRead(@PathVariable UUID userUuid) {
        List<ReadingEntity> readings = readingService.getBooksToRead(userUuid);
        return ResponseEntity.ok(readings);
    }

    /**
     * Récupère les livres "En cours de lecture" d'un utilisateur
     * GET /api/reading/user/{userUuid}/currently-reading
     */
    @GetMapping("/user/{userUuid}/currently-reading")
    public ResponseEntity<List<ReadingEntity>> getBooksCurrentlyReading(@PathVariable UUID userUuid) {
        List<ReadingEntity> readings = readingService.getBooksCurrentlyReading(userUuid);
        return ResponseEntity.ok(readings);
    }

    /**
     * Récupère les livres "Lus" d'un utilisateur
     * GET /api/reading/user/{userUuid}/read
     */
    @GetMapping("/user/{userUuid}/read")
    public ResponseEntity<List<ReadingEntity>> getBooksRead(@PathVariable UUID userUuid) {
        List<ReadingEntity> readings = readingService.getBooksRead(userUuid);
        return ResponseEntity.ok(readings);
    }

    /**
     * Récupère le statut de lecture d'un livre pour un utilisateur
     * GET /api/reading/{userUuid}/{bookIsbn}/status
     */
    @GetMapping("/{userUuid}/{bookIsbn}/status")
    public ResponseEntity<?> getReadingStatus(
            @PathVariable UUID userUuid,
            @PathVariable String bookIsbn) {
        Optional<ReadingStatus> status = readingService.getReadingStatus(userUuid, bookIsbn);
        
        if (status.isPresent()) {
            return ResponseEntity.ok(Map.of("status", status.get()));
        } else {
            return ResponseEntity.ok(Map.of("status", "NOT_IN_LIST"));
        }
    }

    /**
     * Vérifie si un utilisateur a un livre dans sa liste de lecture
     * GET /api/reading/{userUuid}/{bookIsbn}/exists
     */
    @GetMapping("/{userUuid}/{bookIsbn}/exists")
    public ResponseEntity<Map<String, Boolean>> hasBookInReading(
            @PathVariable UUID userUuid,
            @PathVariable String bookIsbn) {
        boolean exists = readingService.hasBookInReading(userUuid, bookIsbn);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Récupère les statistiques de lecture d'un utilisateur
     * GET /api/reading/user/{userUuid}/stats
     */
    @GetMapping("/user/{userUuid}/stats")
    public ResponseEntity<ReadingStatsResponse> getUserReadingStats(@PathVariable UUID userUuid) {
        Long booksRead = readingService.getBooksReadCount(userUuid);
        List<ReadingEntity> toRead = readingService.getBooksToRead(userUuid);
        List<ReadingEntity> currentlyReading = readingService.getBooksCurrentlyReading(userUuid);
        
        ReadingStatsResponse stats = new ReadingStatsResponse(
            booksRead,
            (long) toRead.size(),
            (long) currentlyReading.size()
        );
        
        return ResponseEntity.ok(stats);
    }

    // --- Méthode utilitaire pour transformer une ReadingEntity en ReadingDTO ---
    private ReadingDTO toDTO(ReadingEntity entity) {
        ReadingDTO dto = new ReadingDTO();
        dto.setId(entity.getId());
        dto.setReadingDate(entity.getReadingDate());
        dto.setStatus(entity.getStatus());
        if (entity.getUser() != null && entity.getUser().getUuid() != null) {
            dto.setUserUuid(entity.getUser().getUuid().toString());
        }
        if (entity.getBook() != null && entity.getBook().getIsbn() != null) {
            dto.setBookIsbn(entity.getBook().getIsbn());
        }
        return dto;
    }

    // --- Classes internes pour les DTOs ---

    /**
     * DTO pour ajouter un livre à la liste de lecture
     */
    public static class AddBookToReadingRequest {
        private UUID userUuid;
        private String bookIsbn;
        private ReadingStatus status;

        // Getters & Setters
        public UUID getUserUuid() { return userUuid; }
        public void setUserUuid(UUID userUuid) { this.userUuid = userUuid; }

        public String getBookIsbn() { return bookIsbn; }
        public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }
    }

    /**
     * DTO pour mettre à jour le statut de lecture
     */
    public static class UpdateReadingStatusRequest {
        private ReadingStatus status;

        // Getters & Setters
        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }
    }

    /**
     * DTO pour les statistiques de lecture d'un utilisateur
     */
    public static class ReadingStatsResponse {
        private Long booksRead;
        private Long booksToRead;
        private Long booksCurrentlyReading;

        public ReadingStatsResponse(Long booksRead, Long booksToRead, Long booksCurrentlyReading) {
            this.booksRead = booksRead;
            this.booksToRead = booksToRead;
            this.booksCurrentlyReading = booksCurrentlyReading;
        }

        // Getters & Setters
        public Long getBooksRead() { return booksRead; }
        public void setBooksRead(Long booksRead) { this.booksRead = booksRead; }

        public Long getBooksToRead() { return booksToRead; }
        public void setBooksToRead(Long booksToRead) { this.booksToRead = booksToRead; }

        public Long getBooksCurrentlyReading() { return booksCurrentlyReading; }
        public void setBooksCurrentlyReading(Long booksCurrentlyReading) { 
            this.booksCurrentlyReading = booksCurrentlyReading; 
        }
    }
}