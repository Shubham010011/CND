package com.cryptox.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String username;
    private String walletId;
}