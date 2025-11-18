package com.littlebook.auth.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de la sécurité :
 * - route publique accessible sans token
 * - route protégée renvoie 401 sans token
 * - route protégée renvoie 200 avec ID token Firebase valide (mocké)
 * - route protégée renvoie 401 avec ID token Firebase invalide (mocké)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        // Empêche la conf Firebase réelle de chercher un fichier pendant les tests
        "app.firebase.credentials-path=",
        "app.firebase.project-id=test-project"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    // On remplace les beans réels par des mocks injectés dans le filtre
    @MockBean
    private FirebaseApp firebaseApp;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void public_ping_is_open() throws Exception {
        mvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void me_without_token_is_401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_with_valid_token_is_200() throws Exception {
    // Préparation
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("uid123");
        when(token.getEmail()).thenReturn("john@doe.com");
        when(token.getName()).thenReturn("John Doe");
        when(token.getPicture()).thenReturn("https://pic.example/avatar.png");

        // Le filtre doit utiliser ce mock injecté
        when(firebaseAuth.verifyIdToken("good")).thenReturn(token);

    // Exécution + vérifications
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer good"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.uid").value("uid123"))
                .andExpect(jsonPath("$.email").value("john@doe.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void me_with_invalid_token_is_401() throws Exception {
        // On jette un mock de FirebaseAuthException (pas besoin de constructeur réel)
        FirebaseAuthException authEx = mock(FirebaseAuthException.class);
        when(firebaseAuth.verifyIdToken("bad")).thenThrow(authEx);

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }
}
