package com.cryptox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String walletId;

    public AuthResponse(String token, String username, String walletId) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.walletId = walletId;
    }
}
