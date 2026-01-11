package com.littlebook.auth.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.littlebook.auth.client.AdminServiceClient;
import com.littlebook.auth.client.UserServiceClient;
import com.littlebook.auth.dto.CreateUserRequest;
import com.littlebook.auth.dto.LoginRecordRequest;
import com.littlebook.auth.dto.UserResponse;
import com.littlebook.auth.enums.AuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FirebaseTokenFilter - vérifie la synchronisation avec user-service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FirebaseTokenFilterTest {

    @Autowired
    private FirebaseTokenFilter filter;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private FirebaseApp firebaseApp;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private AdminServiceClient adminServiceClient;

    @Test
    void successful_google_login_syncs_user_to_user_service() throws Exception {
        // Arrange
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("google-uid-123");
        when(token.getEmail()).thenReturn("user@gmail.com");
        when(token.getName()).thenReturn("Google User");
        when(token.getPicture()).thenReturn("https://example.com/pic.jpg");
        when(token.isEmailVerified()).thenReturn(true);
        when(token.getClaims()).thenReturn(Map.of("firebase", Map.of("sign_in_provider", "google.com")));

        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(token);
        when(userServiceClient.syncUser(any())).thenReturn(java.util.Optional.of(
            new UserResponse(java.util.UUID.fromString("00000000-0000-0000-0000-000000000123"),
                "user@gmail.com", "Google User", "https://example.com/pic.jpg",
                AuthProvider.GOOGLE, "google-uid-123", true, "ROLE_USER",
                null, null, null, true)
        ));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

    // Assert - vérifie que userServiceClient a été appelé
    ArgumentCaptor<CreateUserRequest> requestCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
    ArgumentCaptor<LoginRecordRequest> loginCaptor = ArgumentCaptor.forClass(LoginRecordRequest.class);
    verify(userServiceClient, times(1)).syncUser(requestCaptor.capture());
    verify(adminServiceClient, times(1)).recordLogin(eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000123")), loginCaptor.capture());

    CreateUserRequest capturedRequest = requestCaptor.getValue();
        assertEquals(AuthProvider.GOOGLE, capturedRequest.provider());
        assertEquals("google-uid-123", capturedRequest.providerId());
        assertEquals("user@gmail.com", capturedRequest.email());
        assertEquals("Google User", capturedRequest.name());
        assertEquals("https://example.com/pic.jpg", capturedRequest.picture());
        assertTrue(capturedRequest.emailVerified());
    }

    @Test
    void successful_microsoft_login_syncs_user_to_user_service() throws Exception {
        // Arrange
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("ms-uid-456");
        when(token.getEmail()).thenReturn("user@outlook.com");
        when(token.getName()).thenReturn("MS User");
        when(token.getPicture()).thenReturn(null);
        when(token.isEmailVerified()).thenReturn(false);
        when(token.getClaims()).thenReturn(Map.of("firebase", Map.of("sign_in_provider", "microsoft.com")));

        when(firebaseAuth.verifyIdToken("valid-ms-token")).thenReturn(token);
        when(userServiceClient.syncUser(any())).thenReturn(java.util.Optional.of(
            new UserResponse(java.util.UUID.fromString("00000000-0000-0000-0000-000000000456"),
                "user@outlook.com", "MS User", null,
                AuthProvider.MICROSOFT, "ms-uid-456", false, "ROLE_USER",
                null, null, null, true)
        ));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-ms-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
    ArgumentCaptor<CreateUserRequest> requestCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
    ArgumentCaptor<LoginRecordRequest> loginCaptor = ArgumentCaptor.forClass(LoginRecordRequest.class);
    verify(userServiceClient, times(1)).syncUser(requestCaptor.capture());
    verify(adminServiceClient, times(1)).recordLogin(eq(java.util.UUID.fromString("00000000-0000-0000-0000-000000000456")), loginCaptor.capture());

    CreateUserRequest capturedRequest = requestCaptor.getValue();
        assertEquals(AuthProvider.MICROSOFT, capturedRequest.provider());
        assertEquals("ms-uid-456", capturedRequest.providerId());
        assertEquals("user@outlook.com", capturedRequest.email());
        assertEquals("MS User", capturedRequest.name());
        assertNull(capturedRequest.picture());
        assertFalse(capturedRequest.emailVerified());
    }

    @Test
    void rejected_provider_does_not_sync_user() throws Exception {
        // Arrange - provider non autorisé (github.com)
        FirebaseToken token = mock(FirebaseToken.class);
        lenient().when(token.getUid()).thenReturn("gh-uid");
        lenient().when(token.getEmail()).thenReturn("user@github.com");
        lenient().when(token.getName()).thenReturn("GH User");
        lenient().when(token.isEmailVerified()).thenReturn(true);
        when(token.getClaims()).thenReturn(Map.of("firebase", Map.of("sign_in_provider", "github.com")));

        when(firebaseAuth.verifyIdToken("github-token")).thenReturn(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer github-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert - userServiceClient ne doit PAS être appelé
        verify(userServiceClient, never()).syncUser(any());
        verify(adminServiceClient, never()).recordLogin(any(), any());
    }

    @Test
    void no_authorization_header_does_not_sync_user() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        verify(firebaseAuth, never()).verifyIdToken(anyString());
        verify(userServiceClient, never()).syncUser(any());
        verify(adminServiceClient, never()).recordLogin(any(), any());
    }
}
