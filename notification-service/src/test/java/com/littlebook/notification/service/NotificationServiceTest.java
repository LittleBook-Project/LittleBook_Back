package com.littlebook.notification.service;

import com.littlebook.notification.entity.NotificationEntity;
import com.littlebook.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @InjectMocks
    private NotificationService service;

    private NotificationEntity validNotification;

    @BeforeEach
    void setUp() {
        validNotification = new NotificationEntity();
        validNotification.setUserUuid("11111111-1111-1111-1111-111111111111");
        validNotification.setType("REVIEW_CREATED");
        validNotification.setTitle("Review publiée");
        validNotification.setMessage("Votre review sur Le Petit Prince a été publiée.");
        validNotification.setReadFlag(false);
    }

    // --------- ping ---------

    @Test
    void ping_shouldReturnPong() {
        assertEquals("pong", service.ping());
    }

    // --------- create ---------

    @Test
    void create_shouldSaveNotification_whenValid() {
        // Arrange
        NotificationEntity saved = new NotificationEntity();
        UUID id = UUID.randomUUID();
        saved.setId(id);
        saved.setUserUuid(validNotification.getUserUuid());
        saved.setType(validNotification.getType());
        saved.setTitle(validNotification.getTitle());
        saved.setMessage(validNotification.getMessage());
        saved.setReadFlag(false);

        when(repo.save(any(NotificationEntity.class))).thenReturn(saved);

        // Act
        NotificationEntity result = service.create(validNotification);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("REVIEW_CREATED", result.getType());
        verify(repo, times(1)).save(any(NotificationEntity.class));

        // On ne fait jamais confiance à l'id envoyé par le client
        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(repo).save(captor.capture());
        assertNull(captor.getValue().getId(), "Service must nullify client-provided id");
    }

    @Test
    void create_shouldThrow_whenUserUuidMissing() {
        validNotification.setUserUuid("   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(validNotification));
        assertTrue(ex.getMessage().toLowerCase().contains("useruuid"));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenTypeMissing() {
        validNotification.setType(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(validNotification));
        assertTrue(ex.getMessage().toLowerCase().contains("type"));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenTitleMissing() {
        validNotification.setTitle("");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(validNotification));
        assertTrue(ex.getMessage().toLowerCase().contains("title"));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenMessageMissing() {
        validNotification.setMessage(" ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(validNotification));
        assertTrue(ex.getMessage().toLowerCase().contains("message"));
        verify(repo, never()).save(any());
    }

    // --------- getById ---------

    @Test
    void getById_shouldReturnNotification_whenExists() {
        UUID id = UUID.randomUUID();
        NotificationEntity n = new NotificationEntity();
        n.setId(id);
        n.setUserUuid(validNotification.getUserUuid());
        n.setType(validNotification.getType());
        n.setTitle(validNotification.getTitle());
        n.setMessage(validNotification.getMessage());

        when(repo.findById(id)).thenReturn(Optional.of(n));

        NotificationEntity result = service.getById(id);

        assertEquals(id, result.getId());
        verify(repo).findById(id);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getById(id));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(repo).findById(id);
    }

    // --------- listByUser ---------

    @Test
    void listByUser_shouldUseUnreadQuery_whenUnreadOnlyTrue() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        when(repo.findByUserUuidAndReadFlagFalse(eq("11111111-1111-1111-1111-111111111111"), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<NotificationEntity> result = service.listByUser("11111111-1111-1111-1111-111111111111", true, pageable);

        assertNotNull(result);
        verify(repo).findByUserUuidAndReadFlagFalse(eq("11111111-1111-1111-1111-111111111111"), eq(pageable));
        verify(repo, never()).findByUserUuid(anyString(), any());
    }

    @Test
    void listByUser_shouldUseAllQuery_whenUnreadOnlyFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repo.findByUserUuid(eq("11111111-1111-1111-1111-111111111111"), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<NotificationEntity> result = service.listByUser("11111111-1111-1111-1111-111111111111", false, pageable);

        assertNotNull(result);
        verify(repo).findByUserUuid(eq("11111111-1111-1111-1111-111111111111"), eq(pageable));
        verify(repo, never()).findByUserUuidAndReadFlagFalse(anyString(), any());
    }

    // --------- markRead ---------

    @Test
    void markRead_shouldSetReadFlagTrue_andSave() {
        UUID id = UUID.randomUUID();
        NotificationEntity n = new NotificationEntity();
        n.setId(id);
        n.setUserUuid(validNotification.getUserUuid());
        n.setType(validNotification.getType());
        n.setTitle(validNotification.getTitle());
        n.setMessage(validNotification.getMessage());
        n.setReadFlag(false);

        when(repo.findById(id)).thenReturn(Optional.of(n));
        when(repo.save(any(NotificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationEntity result = service.markRead(id);

        assertTrue(result.isReadFlag());
        verify(repo).findById(id);
        verify(repo).save(any(NotificationEntity.class));
    }

    // --------- unreadCount ---------

    @Test
    void unreadCount_shouldDelegateToRepository() {
        when(repo.countByUserUuidAndReadFlagFalse("11111111-1111-1111-1111-111111111111")).thenReturn(3L);

        long count = service.unreadCount("11111111-1111-1111-1111-111111111111");

        assertEquals(3L, count);
        verify(repo).countByUserUuidAndReadFlagFalse("11111111-1111-1111-1111-111111111111");
    }

    // --------- delete ---------

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repo).deleteById(id);
    }
}
