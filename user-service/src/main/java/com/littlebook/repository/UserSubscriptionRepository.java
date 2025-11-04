package com.littlebook.repository;

import com.littlebook.entity.UserSubscriptionEntity;
import com.littlebook.entity.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionId> {

    /**
     * Trouve tous les abonnements d'un utilisateur (les utilisateurs qu'il suit)
     */
    @Query("SELECT s FROM UserSubscriptionEntity s WHERE s.id.subscriberUuid = :subscriberUuid")
    List<UserSubscriptionEntity> findBySubscriberUuid(@Param("subscriberUuid") UUID subscriberUuid);

    /**
     * Trouve tous les abonnés d'un utilisateur (ceux qui le suivent)
     */
    @Query("SELECT s FROM UserSubscriptionEntity s WHERE s.id.subscribedToUuid = :subscribedToUuid")
    List<UserSubscriptionEntity> findBySubscribedToUuid(@Param("subscribedToUuid") UUID subscribedToUuid);

    /**
     * Compte le nombre d'abonnements d'un utilisateur
     */
    @Query("SELECT COUNT(s) FROM UserSubscriptionEntity s WHERE s.id.subscriberUuid = :subscriberUuid")
    Long countBySubscriberUuid(@Param("subscriberUuid") UUID subscriberUuid);

    /**
     * Compte le nombre d'abonnés d'un utilisateur
     */
    @Query("SELECT COUNT(s) FROM UserSubscriptionEntity s WHERE s.id.subscribedToUuid = :subscribedToUuid")
    Long countBySubscribedToUuid(@Param("subscribedToUuid") UUID subscribedToUuid);

    /**
     * Vérifie si un utilisateur suit un autre
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM UserSubscriptionEntity s WHERE s.id.subscriberUuid = :subscriberUuid AND s.id.subscribedToUuid = :subscribedToUuid")
    boolean existsBySubscriberUuidAndSubscribedToUuid(@Param("subscriberUuid") UUID subscriberUuid, @Param("subscribedToUuid") UUID subscribedToUuid);
}
