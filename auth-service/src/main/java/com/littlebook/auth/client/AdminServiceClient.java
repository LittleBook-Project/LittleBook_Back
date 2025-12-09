package com.littlebook.auth.client;

import com.littlebook.auth.dto.LoginRecordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

/**
 * Client pour envoyer les événements de connexion vers admin-service (statistiques).
 */
@Component
public class AdminServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceClient.class);
    private final WebClient webClient;

    public AdminServiceClient(WebClient.Builder builder,
                              @Value("${app.admin-service.url:http://localhost:8085}") String adminServiceUrl) {
        this.webClient = builder.baseUrl(adminServiceUrl).build();
    }

    public void recordLogin(UUID userId, LoginRecordRequest req) {
        try {
            webClient.post()
                    .uri("/api/stats/users/{id}/login", userId)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            log.debug("Login recorded in admin-service for user {}", userId);
        } catch (Exception e) {
            // Ne pas bloquer l'authentification si le service de stats est indisponible
            log.warn("Failed to record login in admin-service for user {}: {}", userId, e.getMessage());
        }
    }
}
