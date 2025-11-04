package com.littlebook.service;

import com.littlebook.entity.UserEntity;
import com.littlebook.entity.UserSubscriptionEntity;
import com.littlebook.entity.UserSubscriptionId;
import com.littlebook.repository.UserRepository;
import com.littlebook.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les abonnements entre utilisateurs
 */
@Service
public class SubscriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    
    @Autowired
    private UserSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Créer un abonnement (un utilisateur suit un autre)
     */
    public UserSubscriptionEntity subscribe(UUID subscriberUuid, UUID subscribedToUuid) {
        // Vérifier que les deux utilisateurs existent
        Optional<UserEntity> subscriber = userRepository.findById(subscriberUuid);
        if (subscriber.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur abonné introuvable");
        }

        Optional<UserEntity> subscribedTo = userRepository.findById(subscribedToUuid);
        if (subscribedTo.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur cible introuvable");
        }

        // Vérifier qu'on ne s'abonne pas à soi-même
        if (subscriberUuid.equals(subscribedToUuid)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous abonner à vous-même");
        }

        // Vérifier que l'abonnement n'existe pas déjà
        UserSubscriptionId id = new UserSubscriptionId(subscriberUuid, subscribedToUuid);
        if (subscriptionRepository.existsById(id)) {
            throw new IllegalArgumentException("Vous êtes déjà abonné à cet utilisateur");
        }

        // Créer l'abonnement
        UserSubscriptionEntity subscription = new UserSubscriptionEntity();
        subscription.setId(id);
        subscription.setSubscriber(subscriber.get());
        subscription.setSubscribedTo(subscribedTo.get());

        UserSubscriptionEntity saved = subscriptionRepository.save(subscription);
        logger.info("✅ Abonnement créé : {} suit {}", subscriberUuid, subscribedToUuid);
        
        return saved;
    }

    /**
     * Se désabonner d'un utilisateur
     */
    public void unsubscribe(UUID subscriberUuid, UUID subscribedToUuid) {
        UserSubscriptionId id = new UserSubscriptionId(subscriberUuid, subscribedToUuid);
        
        if (!subscriptionRepository.existsById(id)) {
            throw new IllegalArgumentException("Cet abonnement n'existe pas");
        }

        subscriptionRepository.deleteById(id);
        logger.info("✅ Désabonnement : {} ne suit plus {}", subscriberUuid, subscribedToUuid);
    }

    /**
     * Obtenir la liste des utilisateurs suivis par un utilisateur
     */
    public List<UserSubscriptionEntity> getFollowing(UUID subscriberUuid) {
        return subscriptionRepository.findBySubscriberUuid(subscriberUuid);
    }

    /**
     * Obtenir la liste des abonnés d'un utilisateur
     */
    public List<UserSubscriptionEntity> getFollowers(UUID subscribedToUuid) {
        return subscriptionRepository.findBySubscribedToUuid(subscribedToUuid);
    }

    /**
     * Vérifier si un utilisateur suit un autre
     */
    public boolean isFollowing(UUID subscriberUuid, UUID subscribedToUuid) {
        return subscriptionRepository.existsBySubscriberUuidAndSubscribedToUuid(subscriberUuid, subscribedToUuid);
    }

    /**
     * Obtenir le nombre d'abonnements d'un utilisateur
     */
    public Long getFollowingCount(UUID subscriberUuid) {
        return subscriptionRepository.countBySubscriberUuid(subscriberUuid);
    }

    /**
     * Obtenir le nombre d'abonnés d'un utilisateur
     */
    public Long getFollowersCount(UUID subscribedToUuid) {
        return subscriptionRepository.countBySubscribedToUuid(subscribedToUuid);
    }
}
