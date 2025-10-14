package com.littlebook.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_subscription")
public class UserSubscriptionEntity implements Serializable {

    @EmbeddedId
    private UserSubscriptionId id;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subscriberUuid")
    @JoinColumn(name = "subscriber_uuid", nullable = false, referencedColumnName = "uuid")
    private UserEntity subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subscribedToUuid")
    @JoinColumn(name = "subscribed_to_uuid", nullable = false, referencedColumnName = "uuid")
    private UserEntity subscribedTo;

    // Getters & Setters
    public UserSubscriptionId getId() {
        return id;
    }
    public void setId(UserSubscriptionId id) {
        this.id = id;
    }

    public UserEntity getSubscriber() {
        return subscriber;
    }
    public void setSubscriber(UserEntity subscriber) {
        this.subscriber = subscriber;
    }

    public UserEntity getSubscribedTo() {
        return subscribedTo;
    }
    public void setSubscribedTo(UserEntity subscribedTo) {
        this.subscribedTo = subscribedTo;
    }
}