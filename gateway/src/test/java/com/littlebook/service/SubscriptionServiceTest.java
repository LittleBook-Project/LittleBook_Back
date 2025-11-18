package com.littlebook.service;

import com.littlebook.entity.UserEntity;
import com.littlebook.entity.UserSubscriptionEntity;
import com.littlebook.entity.UserSubscriptionId;
import com.littlebook.repository.UserRepository;
import com.littlebook.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour SubscriptionService
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private UUID subscriberUuid;
    private UUID subscribedToUuid;
    private UserEntity subscriber;
    private UserEntity subscribedTo;

    @BeforeEach
    void setUp() {
        subscriberUuid = UUID.randomUUID();
        subscribedToUuid = UUID.randomUUID();

        subscriber = new UserEntity();
        subscriber.setUuid(subscriberUuid);
        subscriber.setUserName("subscriber_user");

        subscribedTo = new UserEntity();
        subscribedTo.setUuid(subscribedToUuid);
        subscribedTo.setUserName("followed_user");
    }

    @Test
    void subscribe_Success() {
        // Given
        when(userRepository.findById(subscriberUuid)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(subscribedToUuid)).thenReturn(Optional.of(subscribedTo));
        when(subscriptionRepository.existsById(any(UserSubscriptionId.class))).thenReturn(false);
        
        UserSubscriptionEntity savedSubscription = new UserSubscriptionEntity();
        savedSubscription.setId(new UserSubscriptionId(subscriberUuid, subscribedToUuid));
        savedSubscription.setSubscriber(subscriber);
        savedSubscription.setSubscribedTo(subscribedTo);
        
        when(subscriptionRepository.save(any(UserSubscriptionEntity.class))).thenReturn(savedSubscription);

        // When
        UserSubscriptionEntity result = subscriptionService.subscribe(subscriberUuid, subscribedToUuid);

        // Then
        assertNotNull(result);
        verify(subscriptionRepository, times(1)).save(any(UserSubscriptionEntity.class));
    }

    @Test
    void subscribe_SubscriberNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(subscriberUuid)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> subscriptionService.subscribe(subscriberUuid, subscribedToUuid)
        );
        
        assertEquals("Utilisateur abonné introuvable", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_SubscribedToNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(subscriberUuid)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(subscribedToUuid)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> subscriptionService.subscribe(subscriberUuid, subscribedToUuid)
        );
        
        assertEquals("Utilisateur cible introuvable", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_SelfSubscribe_ThrowsException() {
        // Given
        UUID sameUuid = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setUuid(sameUuid);

        when(userRepository.findById(sameUuid)).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> subscriptionService.subscribe(sameUuid, sameUuid)
        );
        
        assertEquals("Vous ne pouvez pas vous abonner à vous-même", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_AlreadySubscribed_ThrowsException() {
        // Given
        when(userRepository.findById(subscriberUuid)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(subscribedToUuid)).thenReturn(Optional.of(subscribedTo));
        when(subscriptionRepository.existsById(any(UserSubscriptionId.class))).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> subscriptionService.subscribe(subscriberUuid, subscribedToUuid)
        );
        
        assertEquals("Vous êtes déjà abonné à cet utilisateur", exception.getMessage());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void unsubscribe_Success() {
        // Given
        when(subscriptionRepository.existsById(any(UserSubscriptionId.class))).thenReturn(true);

        // When
        subscriptionService.unsubscribe(subscriberUuid, subscribedToUuid);

        // Then
        verify(subscriptionRepository, times(1)).deleteById(any(UserSubscriptionId.class));
    }

    @Test
    void unsubscribe_NotSubscribed_ThrowsException() {
        // Given
        when(subscriptionRepository.existsById(any(UserSubscriptionId.class))).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> subscriptionService.unsubscribe(subscriberUuid, subscribedToUuid)
        );
        
        assertEquals("Cet abonnement n'existe pas", exception.getMessage());
        verify(subscriptionRepository, never()).deleteById(any());
    }

    @Test
    void isFollowing_ReturnsTrue() {
        // Given
        when(subscriptionRepository.existsBySubscriberUuidAndSubscribedToUuid(subscriberUuid, subscribedToUuid))
            .thenReturn(true);

        // When
        boolean result = subscriptionService.isFollowing(subscriberUuid, subscribedToUuid);

        // Then
        assertTrue(result);
    }

    @Test
    void isFollowing_ReturnsFalse() {
        // Given
        when(subscriptionRepository.existsBySubscriberUuidAndSubscribedToUuid(subscriberUuid, subscribedToUuid))
            .thenReturn(false);

        // When
        boolean result = subscriptionService.isFollowing(subscriberUuid, subscribedToUuid);

        // Then
        assertFalse(result);
    }

    @Test
    void getFollowingCount_ReturnsCorrectCount() {
        // Given
        when(subscriptionRepository.countBySubscriberUuid(subscriberUuid)).thenReturn(5L);

        // When
        Long count = subscriptionService.getFollowingCount(subscriberUuid);

        // Then
        assertEquals(5L, count);
    }

    @Test
    void getFollowersCount_ReturnsCorrectCount() {
        // Given
        when(subscriptionRepository.countBySubscribedToUuid(subscribedToUuid)).thenReturn(10L);

        // When
        Long count = subscriptionService.getFollowersCount(subscribedToUuid);

        // Then
        assertEquals(10L, count);
    }
}
