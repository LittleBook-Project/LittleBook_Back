package com.littlebook.auth.security;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que la requête préflight OPTIONS renvoie les headers CORS attendus
 * (origin autorisée, allow-credentials=true, allow-methods inclut la méthode demandée).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        // Empêche FirebaseConfig de tenter de charger un fichier réel pendant les tests
        "app.firebase.credentials-path=",
        "app.firebase.project-id=test-project"
})
@AutoConfigureMockMvc
@org.springframework.boot.test.mock.mockito.MockBean(com.google.firebase.FirebaseApp.class)
@org.springframework.boot.test.mock.mockito.MockBean(com.google.firebase.auth.FirebaseAuth.class)
public class CorsPreflightTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void preflightOptions_fromAllowedOrigin_returnsCorsHeaders() throws Exception {
        mvc.perform(options("/api/auth/me")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", Matchers.containsString("GET")));
    }

    @Test
    void preflightOptions_fromNotAllowedOrigin_doesNotReturnAllowOrigin() throws Exception {
    mvc.perform(options("/api/auth/me")
            .header("Origin", "http://evil.example")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
    // S'assurer que Access-Control-Allow-Origin N'EST PAS présent pour une origine non autorisée
    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void preflightOptions_allowedOrigin_returnsAllowHeaders() throws Exception {
        mvc.perform(options("/api/auth/me")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Headers", Matchers.containsString("Authorization")))
                .andExpect(header().string("Access-Control-Allow-Headers", Matchers.containsString("Content-Type")));
    }
}
