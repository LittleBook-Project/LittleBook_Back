package com.littlebook.auth.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.littlebook.auth.client.UserServiceClient;
import com.littlebook.auth.dto.CreateUserRequest;
import com.littlebook.auth.enums.AuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@ExtendWith(MockitoExtension.class)
class FirebaseTokenFilterTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private FirebaseTokenFilter filter;

    @Captor
    private ArgumentCaptor<CreateUserRequest> requestCaptor;

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

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert - vérifie que userServiceClient a été appelé
        verify(userServiceClient, times(1)).syncUser(requestCaptor.capture());
        
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

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-ms-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        verify(userServiceClient, times(1)).syncUser(requestCaptor.capture());
        
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
    }
}
