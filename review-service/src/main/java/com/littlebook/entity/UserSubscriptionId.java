package com.littlebook.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

// Clé primaire composite pour identifier de manière unique un abonnement entre deux utilisateurs

@Embeddable
public class UserSubscriptionId implements Serializable {

    @Column(name = "subscriber_uuid")
    private UUID subscriberUuid;

    @Column(name = "subscribed_to_uuid")
    private UUID subscribedToUuid;

    // Constructeurs
    public UserSubscriptionId() {}

    public UserSubscriptionId(UUID subscriberUuid, UUID subscribedToUuid) {
        this.subscriberUuid = subscriberUuid;
        this.subscribedToUuid = subscribedToUuid;
    }

    // Getters & Setters
    public UUID getSubscriberUuid() {
        return subscriberUuid;
    }
    public void setSubscriberUuid(UUID subscriberUuid) {
        this.subscriberUuid = subscriberUuid;
    }

    public UUID getSubscribedToUuid() {
        return subscribedToUuid;
    }
    public void setSubscribedToUuid(UUID subscribedToUuid) {
        this.subscribedToUuid = subscribedToUuid;
    }

    // equals & hashCode (obligatoires pour les clés composites)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSubscriptionId that = (UserSubscriptionId) o;
        return Objects.equals(subscriberUuid, that.subscriberUuid)
                && Objects.equals(subscribedToUuid, that.subscribedToUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberUuid, subscribedToUuid);
    }
}
