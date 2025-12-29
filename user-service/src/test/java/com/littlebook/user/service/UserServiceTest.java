package com.littlebook.user.service;
import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.dto.UpdateUserRequest;
import com.littlebook.user.entity.User;
import com.littlebook.user.enums.AuthProvider;
import com.littlebook.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


<<<<<<< HEAD




=======
>>>>>>> d0efca2f7b4792bd4f1e519a36c86f3a1165f275
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void pingReturnsPong() {
        assertEquals("pong", userService.ping());
    }

    @Test
    void findByIdDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        User u = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(u));

        Optional<User> res = userService.findById(id);

        assertTrue(res.isPresent());
        assertSame(u, res.get());
        verify(userRepository).findById(id);
    }

    @Test
    void findByEmailDelegatesToRepository() {
        String email = "a@b.com";
        User u = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

        Optional<User> res = userService.findByEmail(email);

        assertTrue(res.isPresent());
        assertSame(u, res.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByProviderAndProviderIdDelegatesToRepository() {
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "pid"))
                .thenReturn(Optional.empty());

        Optional<User> res = userService.findByProviderAndProviderId(AuthProvider.GOOGLE, "pid");

        assertFalse(res.isPresent());
        verify(userRepository).findByProviderAndProviderId(AuthProvider.GOOGLE, "pid");
    }

    @Test
    void getOrCreateFromOAuth_updatesExistingByProvider() {
        CreateUserRequest req = mock(CreateUserRequest.class);
        when(req.provider()).thenReturn(AuthProvider.GOOGLE);
        when(req.providerId()).thenReturn("prov-1");
        when(req.name()).thenReturn("New Name");
        when(req.picture()).thenReturn("pic-url");
        when(req.emailVerified()).thenReturn(true);

        User existing = new User();
        existing.setName("Old");
        existing.setPicture("old-pic");
        existing.setEmailVerified(false);

        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "prov-1"))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        User res = userService.getOrCreateFromOAuth(req);

        assertSame(existing, res);
        assertEquals("New Name", res.getName());
        assertEquals("pic-url", res.getPicture());
        assertTrue(res.isEmailVerified());
        assertNotNull(res.getLastLogin());
        verify(userRepository).save(existing);
    }

    @Test
    void getOrCreateFromOAuth_linksByEmailWhenProviderMissing() {
        CreateUserRequest req = mock(CreateUserRequest.class);
        when(req.provider()).thenReturn(AuthProvider.MICROSOFT);
        when(req.providerId()).thenReturn("ms-123");
        when(req.name()).thenReturn("Provided Name");
        when(req.picture()).thenReturn("provided-pic");
        when(req.email()).thenReturn("u@e.com");
        when(req.emailVerified()).thenReturn(true);

        when(userRepository.findByProviderAndProviderId(AuthProvider.MICROSOFT, "ms-123"))
                .thenReturn(Optional.empty());

        User existing = new User();
        existing.setEmail("u@e.com");
        // ensure existing has null name and picture to be filled
        existing.setName(null);
        existing.setPicture(null);

        when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        User res = userService.getOrCreateFromOAuth(req);

        assertSame(existing, res);
        assertEquals(AuthProvider.MICROSOFT, res.getProvider());
        assertEquals("ms-123", res.getProviderId());
        assertEquals("Provided Name", res.getName());
        assertEquals("provided-pic", res.getPicture());
        assertTrue(res.isEmailVerified());
        assertNotNull(res.getLastLogin());
        verify(userRepository).save(existing);
    }

    @Test
    void getOrCreateFromOAuth_createsNewWhenNoMatch() {
        CreateUserRequest req = mock(CreateUserRequest.class);
        when(req.provider()).thenReturn(AuthProvider.GOOGLE);
        when(req.providerId()).thenReturn("new-prov");
        when(req.name()).thenReturn("NameX");
        when(req.picture()).thenReturn("picX");
        when(req.email()).thenReturn("nx@e.com");
        when(req.emailVerified()).thenReturn(false);

        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "new-prov"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("nx@e.com")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User res = userService.getOrCreateFromOAuth(req);

        assertEquals("nx@e.com", res.getEmail());
        assertEquals("NameX", res.getName());
        assertEquals("picX", res.getPicture());
        assertEquals(AuthProvider.GOOGLE, res.getProvider());
        assertEquals("new-prov", res.getProviderId());
        assertFalse(res.isEmailVerified());
        assertEquals("ROLE_USER", res.getRoles());
        assertNotNull(res.getCreatedAt());
        assertNotNull(res.getLastLogin());
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("ROLE_USER", saved.getRoles());
    }

    @Test
    void updateProfile_updatesFieldsWhenPresent() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setName("old");
        existing.setPicture("old-pic");
        existing.setRoles("ROLE_USER");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        UpdateUserRequest req = mock(UpdateUserRequest.class);
        when(req.name()).thenReturn("new-name");
        when(req.picture()).thenReturn(null);
        when(req.roles()).thenReturn("ROLE_ADMIN");

        User res = userService.updateProfile(id, req);

        assertEquals("new-name", res.getName());
        assertEquals("old-pic", res.getPicture()); // unchanged
        assertEquals("ROLE_ADMIN", res.getRoles());
        verify(userRepository).save(existing);
    }

    @Test
    void updateProfile_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UpdateUserRequest req = mock(UpdateUserRequest.class);

        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(id, req));
    }

    @Test
    void deactivate_setsActiveFalseAndSaves() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setActive(true);

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(i -> i.getArgument(0));

        userService.deactivate(id);

        assertFalse(existing.isActive());
        verify(userRepository).save(existing);
    }
}