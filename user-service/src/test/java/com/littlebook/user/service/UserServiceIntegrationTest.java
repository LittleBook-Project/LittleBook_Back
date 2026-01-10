package com.littlebook.user.service;

import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.entity.User;
import com.littlebook.user.enums.AuthProvider;
import com.littlebook.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour UserService - vérification de la création et mise à jour d'utilisateurs OAuth
 */
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void getOrCreateFromOAuth_creates_new_user_on_first_login() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-123",
                "newuser@gmail.com",
                "New User",
                "https://example.com/pic.jpg",
                true
        );

        // Act
        User user = userService.getOrCreateFromOAuth(request);

        // Assert
        assertNotNull(user.getId());
        assertEquals("newuser@gmail.com", user.getEmail());
        assertEquals("New User", user.getName());
        assertEquals("https://example.com/pic.jpg", user.getPicture());
        assertEquals(AuthProvider.GOOGLE, user.getProvider());
        assertEquals("google-uid-123", user.getProviderId());
        assertTrue(user.isEmailVerified());
        assertNotNull(user.getLastLogin());
        assertNotNull(user.getCreatedAt());
        assertEquals("ROLE_USER", user.getRoles());
        assertTrue(user.isActive());
    }

    @Test
    void getOrCreateFromOAuth_updates_lastLogin_on_subsequent_login() throws InterruptedException {
        // Arrange - créer un utilisateur existant
        CreateUserRequest initialRequest = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-456",
                "existing@gmail.com",
                "Existing User",
                "https://example.com/old.jpg",
                true
        );
        User initialUser = userService.getOrCreateFromOAuth(initialRequest);
        Instant firstLogin = initialUser.getLastLogin();

        // Attendre un peu pour avoir une différence de temps
        Thread.sleep(10);

        // Act - deuxième login avec des infos mises à jour
        CreateUserRequest secondRequest = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-456",
                "existing@gmail.com",
                "Updated User",
                "https://example.com/new.jpg",
                true
        );
        User updatedUser = userService.getOrCreateFromOAuth(secondRequest);

        // Assert
        assertEquals(initialUser.getId(), updatedUser.getId()); // Même utilisateur
        assertEquals("Updated User", updatedUser.getName()); // Nom mis à jour
        assertEquals("https://example.com/new.jpg", updatedUser.getPicture()); // Photo mise à jour
        assertTrue(updatedUser.getLastLogin().isAfter(firstLogin)); // lastLogin mis à jour
    }

    @Test
    void getOrCreateFromOAuth_links_provider_to_existing_user_by_email() {
        // Arrange - créer un utilisateur avec Google
        CreateUserRequest googleRequest = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-789",
                "shared@example.com",
                "Shared User",
                "https://example.com/google.jpg",
                true
        );
        User googleUser = userService.getOrCreateFromOAuth(googleRequest);

        // Act - même utilisateur se connecte avec Microsoft
        CreateUserRequest msRequest = new CreateUserRequest(
                AuthProvider.MICROSOFT,
                "ms-uid-999",
                "shared@example.com",
                "MS User", // nom différent
                null,
                false
        );
        User linkedUser = userService.getOrCreateFromOAuth(msRequest);

        // Assert
        assertEquals(googleUser.getId(), linkedUser.getId()); // Même utilisateur
        assertEquals(AuthProvider.MICROSOFT, linkedUser.getProvider()); // Provider mis à jour vers MS
        assertEquals("ms-uid-999", linkedUser.getProviderId());
        // Le nom reste celui de Google car il n'était pas null
        assertEquals("Shared User", linkedUser.getName());
    }

    @Test
    void getOrCreateFromOAuth_accepts_microsoft_with_unverified_email() {
        // Arrange
        CreateUserRequest msRequest = new CreateUserRequest(
                AuthProvider.MICROSOFT,
                "ms-uid-unverified",
                "unverified@outlook.com",
                "Unverified User",
                null,
                false // email non vérifié
        );

        // Act
        User user = userService.getOrCreateFromOAuth(msRequest);

        // Assert
        assertNotNull(user.getId());
        assertEquals("unverified@outlook.com", user.getEmail());
        assertEquals(AuthProvider.MICROSOFT, user.getProvider());
        assertFalse(user.isEmailVerified()); // Stocké tel quel
        assertNotNull(user.getLastLogin());
        assertTrue(user.isActive());
    }

    @Test
    void getOrCreateFromOAuth_finds_by_provider_and_providerId() {
        // Arrange - créer deux utilisateurs avec emails différents mais même provider+providerId impossible normalement
        // On va plutôt tester qu'un utilisateur existant avec provider+providerId est retrouvé
        CreateUserRequest request1 = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-same",
                "user1@gmail.com",
                "User One",
                null,
                true
        );
        User user1 = userService.getOrCreateFromOAuth(request1);

        // Act - même provider+providerId, email possiblement changé côté provider
        CreateUserRequest request2 = new CreateUserRequest(
                AuthProvider.GOOGLE,
                "google-uid-same",
                "user1_changed@gmail.com", // Email changé côté Google (rare mais possible)
                "User One Updated",
                "https://new.pic",
                true
        );
        User user2 = userService.getOrCreateFromOAuth(request2);

        // Assert - doit retrouver le même utilisateur par provider+providerId
        assertEquals(user1.getId(), user2.getId());
        // Les infos sont mises à jour
        assertEquals("User One Updated", user2.getName());
        assertEquals("https://new.pic", user2.getPicture());
    }
}
