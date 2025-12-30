package com.littlebook.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

@SpringBootTest(classes = AuthApplication.class)
@MockBean(FirebaseApp.class)
@MockBean(FirebaseAuth.class)
class AuthApplicationTests {
    @Test
    void contextLoads() {
            // Si ça passe, le contexte Spring démarre correctement.
    }
}