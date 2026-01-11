package com.littlebook.auth.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.littlebook.auth.client.AdminServiceClient;
import com.littlebook.auth.client.UserServiceClient;
import com.littlebook.auth.security.FirebaseTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test auto-configuration loaded from test classpath to provide
 * replacement beans for Firebase and external clients so the
 * ApplicationContext can start without real Firebase credentials.
 *
 * This file is under src/test and will not affect production code.
 */
@Configuration
public class AuthTestAutoConfiguration {

    @Bean
    @Primary
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        // Use a real FirebaseAuth instance tied to the test FirebaseApp
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    @Primary
    public FirebaseApp firebaseApp() {
        // If a FirebaseApp is already initialized (possibly by production config), return it.
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        // Create a lightweight test FirebaseApp. We intentionally avoid mocking final SDK
        // classes (which fails on newer JDKs) by creating a real instance with minimal options.
        var options = com.google.firebase.FirebaseOptions.builder()
                .setProjectId("test-project")
                .build();

        // Use a distinct app name to avoid clashing with default instances if any.
        return FirebaseApp.initializeApp(options, "test-app");
    }

    @Bean
    @Primary
    public UserServiceClient userServiceClient() {
        return mock(UserServiceClient.class);
    }

    @Bean
    @Primary
    public AdminServiceClient adminServiceClient() {
        return mock(AdminServiceClient.class);
    }

    @Bean
    public FirebaseTokenFilter firebaseTokenFilter(FirebaseAuth firebaseAuth,
                                                   UserServiceClient userServiceClient,
                                                   AdminServiceClient adminServiceClient) {
        return new FirebaseTokenFilter(firebaseAuth, userServiceClient, adminServiceClient);
    }
}
