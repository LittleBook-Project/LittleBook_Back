package com.littlebook.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    private RestTemplate restTemplate;

    private UserClient userClient;

    private final String base = "http://localhost:8082";

    @Test
    void getUserById_returnsMapWhenWithBody() {
        UUID id = UUID.randomUUID();
        String expectedUrl = String.format("%s/user/%s", base, id.toString());
        // restTemplate that returns a 200 with body
        Map<String, Object> body = Map.of("email", "a@b.com", "name", "Alice");
        restTemplate = new RestTemplate() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
                if (url.equals(expectedUrl)) return (ResponseEntity<T>) ResponseEntity.ok(body);
                return super.getForEntity(url, responseType, uriVariables);
            }
        };
        userClient = new UserClient(restTemplate, base);

        Optional<Map<String, Object>> res = userClient.getUserById(id);

        assertTrue(res.isPresent());
        assertEquals("a@b.com", res.get().get("email"));
    }

    @Test
    void getUserById_returnsEmptyNoBody() {
        UUID id = UUID.randomUUID();
        String expectedUrl = String.format("%s/user/%s", base, id.toString());
        // restTemplate that returns 200 with null body
        restTemplate = new RestTemplate() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
                if (url.equals(expectedUrl)) return (ResponseEntity<T>) new ResponseEntity<>(null, HttpStatus.OK);
                return super.getForEntity(url, responseType, uriVariables);
            }
        };
        userClient = new UserClient(restTemplate, base);

        Optional<Map<String, Object>> res = userClient.getUserById(id);

        assertTrue(res.isEmpty());
    }

    @Test
    void getUserById_returnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        String expectedUrl = String.format("%s/user/%s", base, id.toString());
        // restTemplate that throws NotFound
        restTemplate = new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
                if (url.equals(expectedUrl)) throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
                return super.getForEntity(url, responseType, uriVariables);
            }
        };
        userClient = new UserClient(restTemplate, base);

        Optional<Map<String, Object>> res = userClient.getUserById(id);

        assertTrue(res.isEmpty());
    }

    @Test
    void getUserById_returnsEmptyOnGenericException() {
        UUID id = UUID.randomUUID();
        String expectedUrl = String.format("%s/user/%s", base, id.toString());
        // restTemplate that throws generic exception
        restTemplate = new RestTemplate() {
            @Override
            public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
                if (url.equals(expectedUrl)) throw new RuntimeException("network");
                return super.getForEntity(url, responseType, uriVariables);
            }
        };
        userClient = new UserClient(restTemplate, base);

        Optional<Map<String, Object>> res = userClient.getUserById(id);

        assertTrue(res.isEmpty());
    }
}
