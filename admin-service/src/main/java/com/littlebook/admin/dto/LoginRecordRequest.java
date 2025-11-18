package com.littlebook.admin.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO utilisé par l'API admin pour signaler un événement de connexion.
 * Les champs provider/ipAddress/userAgent sont optionnels mais utiles pour le logging.
 */
public class LoginRecordRequest {

    private String provider;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loggedInAt;

    public LoginRecordRequest() {}

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLoggedInAt() {
        return loggedInAt;
    }

    public void setLoggedInAt(LocalDateTime loggedInAt) {
        this.loggedInAt = loggedInAt;
    }
}
