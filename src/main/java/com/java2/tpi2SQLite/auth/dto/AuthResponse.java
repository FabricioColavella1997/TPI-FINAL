package com.java2.tpi2SQLite.auth.dto;

public class AuthResponse {
    private String token;
    private String expiresAt;

    public AuthResponse() {
    }

    public AuthResponse(String token, String expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
    public AuthResponse(String token) {
        this.token = token;
        this.expiresAt = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}


