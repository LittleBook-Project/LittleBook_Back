package com.littlebook.dto;

import java.util.UUID;

public class SubscriptionDTO {
    private UUID subscriberUuid;
    private UUID subscribedToUuid;
    private String subscriberName;
    private String subscribedToName;

    // Getters & Setters
    public UUID getSubscriberUuid() { return subscriberUuid; }
    public void setSubscriberUuid(UUID subscriberUuid) { this.subscriberUuid = subscriberUuid; }

    public UUID getSubscribedToUuid() { return subscribedToUuid; }
    public void setSubscribedToUuid(UUID subscribedToUuid) { this.subscribedToUuid = subscribedToUuid; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public String getSubscribedToName() { return subscribedToName; }
    public void setSubscribedToName(String subscribedToName) { this.subscribedToName = subscribedToName; }
}
