package com.cryptox.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendCryptoRequest {
    @NotBlank
    private String toWalletId;

    @NotBlank
    private String coinSymbol;

    @NotNull
    @DecimalMin(value = "0.0000000001", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String note;
}
