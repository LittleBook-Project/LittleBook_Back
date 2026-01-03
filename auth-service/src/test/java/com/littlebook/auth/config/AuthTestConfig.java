package com.littlebook.auth.config;

import com.google.firebase.auth.FirebaseAuth;
import com.littlebook.auth.client.AdminServiceClient;
import com.littlebook.auth.client.UserServiceClient;
import com.littlebook.auth.security.FirebaseTokenFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

/**
 * Test configuration to provide fake beans required by Security config during tests.
 * Provides a Mockito mock for FirebaseAuth and a FirebaseTokenFilter constructed
 * with mocks so that the SecurityConfig can be initialized even when
 * the production conditional property is different.
 */
@TestConfiguration
public class AuthTestConfig {

    @Bean
    public FirebaseAuth firebaseAuth() {
        return mock(FirebaseAuth.class);
    }

    @Bean
    public FirebaseTokenFilter firebaseTokenFilter(FirebaseAuth firebaseAuth,
                                                   UserServiceClient userServiceClient,
                                                   AdminServiceClient adminServiceClient) {
        return new FirebaseTokenFilter(firebaseAuth, userServiceClient, adminServiceClient);
    }
}
