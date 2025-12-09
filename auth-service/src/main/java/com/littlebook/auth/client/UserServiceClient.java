package com.littlebook.auth.client;

import com.littlebook.auth.dto.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client pour communiquer avec user-service
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private final WebClient webClient;

    public UserServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.user-service.url:http://localhost:8082}") String userServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    }

    /**
     * Appelle POST /user/oauth pour créer ou mettre à jour un utilisateur
     * @param request les informations de l'utilisateur OAuth
     */
    public void syncUser(CreateUserRequest request) {
        try {
            webClient.post()
                    .uri("/user/oauth")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            log.debug("User synchronized with user-service: {}", request.email());
        } catch (Exception e) {
            // Ne pas bloquer l'authentification si user-service est indisponible
            log.error("Failed to sync user with user-service for email {}: {}", 
                    request.email(), e.getMessage());
        }
    }
}
