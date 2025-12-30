package com.littlebook.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${app.user-service.url:http://user-service:8082}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Récupère un utilisateur depuis le user-service et retourne une Map (JSON) des champs.
     * Utilise Map pour rester découplé du modèle exact du user-service.
     */
    public Optional<Map<String, Object>> getUserById(UUID id) {
        String url = String.format("%s/user/%s", baseUrl, id.toString());
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                //noinspection unchecked
                return Optional.of((Map<String, Object>) resp.getBody());
            }
        } catch (HttpClientErrorException.NotFound nf) {
            return Optional.empty();
        } catch (Exception ex) {
            // Silence network issues here; caller can decide how to handle absent details.
        }
        return Optional.empty();
    }
}
