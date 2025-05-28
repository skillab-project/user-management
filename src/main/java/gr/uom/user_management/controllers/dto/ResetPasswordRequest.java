package gr.uom.user_management.controllers.dto;

import java.util.UUID;

public class ResetPasswordRequest {
    String token;
    String password;
    UUID uuid;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String password, UUID uuid) {
        this.token = token;
        this.password = password;
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}

