package com.littlebook.controller;

import com.littlebook.entity.UserSubscriptionEntity;
import com.littlebook.dto.SubscriptionDTO;
import com.littlebook.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour gérer les abonnements entre utilisateurs
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * S'abonner à un utilisateur
     * POST /api/subscriptions
     */
    @PostMapping
    public ResponseEntity<?> subscribe(@RequestBody SubscribeRequest request) {
        try {
            UserSubscriptionEntity subscription = subscriptionService.subscribe(
                request.getSubscriberUuid(),
                request.getSubscribedToUuid()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(subscription));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Se désabonner d'un utilisateur
     * DELETE /api/subscriptions/{subscriberUuid}/{subscribedToUuid}
     */
    @DeleteMapping("/{subscriberUuid}/{subscribedToUuid}")
    public ResponseEntity<?> unsubscribe(
            @PathVariable UUID subscriberUuid,
            @PathVariable UUID subscribedToUuid) {
        try {
            subscriptionService.unsubscribe(subscriberUuid, subscribedToUuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtenir la liste des utilisateurs suivis par un utilisateur
     * GET /api/subscriptions/{subscriberUuid}/following
     */
    @GetMapping("/{subscriberUuid}/following")
    public ResponseEntity<List<SubscriptionDTO>> getFollowing(@PathVariable UUID subscriberUuid) {
        List<UserSubscriptionEntity> subscriptions = subscriptionService.getFollowing(subscriberUuid);
        List<SubscriptionDTO> dtos = subscriptions.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtenir la liste des abonnés d'un utilisateur
     * GET /api/subscriptions/{subscribedToUuid}/followers
     */
    @GetMapping("/{subscribedToUuid}/followers")
    public ResponseEntity<List<SubscriptionDTO>> getFollowers(@PathVariable UUID subscribedToUuid) {
        List<UserSubscriptionEntity> subscriptions = subscriptionService.getFollowers(subscribedToUuid);
        List<SubscriptionDTO> dtos = subscriptions.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Vérifier si un utilisateur suit un autre
     * GET /api/subscriptions/{subscriberUuid}/follows/{subscribedToUuid}
     */
    @GetMapping("/{subscriberUuid}/follows/{subscribedToUuid}")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @PathVariable UUID subscriberUuid,
            @PathVariable UUID subscribedToUuid) {
        boolean isFollowing = subscriptionService.isFollowing(subscriberUuid, subscribedToUuid);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    /**
     * Obtenir les statistiques d'abonnements d'un utilisateur
     * GET /api/subscriptions/{userUuid}/stats
     */
    @GetMapping("/{userUuid}/stats")
    public ResponseEntity<SubscriptionStatsResponse> getStats(@PathVariable UUID userUuid) {
        Long followingCount = subscriptionService.getFollowingCount(userUuid);
        Long followersCount = subscriptionService.getFollowersCount(userUuid);
        
        SubscriptionStatsResponse stats = new SubscriptionStatsResponse(followingCount, followersCount);
        return ResponseEntity.ok(stats);
    }

    // --- Méthode utilitaire pour transformer une UserSubscriptionEntity en SubscriptionDTO ---
    private SubscriptionDTO toDTO(UserSubscriptionEntity entity) {
        SubscriptionDTO dto = new SubscriptionDTO();
        if (entity.getId() != null) {
            dto.setSubscriberUuid(entity.getId().getSubscriberUuid());
            dto.setSubscribedToUuid(entity.getId().getSubscribedToUuid());
        }
        if (entity.getSubscriber() != null) {
            dto.setSubscriberName(entity.getSubscriber().getUserName());
        }
        if (entity.getSubscribedTo() != null) {
            dto.setSubscribedToName(entity.getSubscribedTo().getUserName());
        }
        return dto;
    }

    // --- Classes internes pour les DTOs ---

    /**
     * DTO pour créer un abonnement
     */
    public static class SubscribeRequest {
        private UUID subscriberUuid;
        private UUID subscribedToUuid;

        // Getters & Setters
        public UUID getSubscriberUuid() { return subscriberUuid; }
        public void setSubscriberUuid(UUID subscriberUuid) { this.subscriberUuid = subscriberUuid; }

        public UUID getSubscribedToUuid() { return subscribedToUuid; }
        public void setSubscribedToUuid(UUID subscribedToUuid) { this.subscribedToUuid = subscribedToUuid; }
    }

    /**
     * DTO pour les statistiques d'abonnements
     */
    public static class SubscriptionStatsResponse {
        private Long followingCount;
        private Long followersCount;

        public SubscriptionStatsResponse(Long followingCount, Long followersCount) {
            this.followingCount = followingCount;
            this.followersCount = followersCount;
        }

        // Getters & Setters
        public Long getFollowingCount() { return followingCount; }
        public void setFollowingCount(Long followingCount) { this.followingCount = followingCount; }

        public Long getFollowersCount() { return followersCount; }
        public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }
    }
}
