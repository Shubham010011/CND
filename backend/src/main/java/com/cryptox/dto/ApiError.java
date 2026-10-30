package com.cryptox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiError {
    private Instant timestamp;
    private int status;
    private String message;

    public ApiError(int status, String message) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
    }
}
