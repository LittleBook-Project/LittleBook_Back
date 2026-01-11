package com.littlebook.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.littlebook.auth.client.UserServiceClient;
import com.littlebook.auth.client.AdminServiceClient;

@SpringBootTest(classes = AuthApplication.class)
class AuthApplicationTests {

    @MockBean
    private FirebaseApp firebaseApp;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private AdminServiceClient adminServiceClient;
    @Test
    void contextLoads() {
            // Si ça passe, le contexte Spring démarre correctement.
    }
}