package com.cryptox.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Trade directly from one coin to another (e.g. BTC -> ETH), priced via each coin's live USD rate. */
@Data
public class SwapRequest {
    @NotBlank
    private String fromCoinSymbol;

    @NotBlank
    private String toCoinSymbol;

    @NotNull
    @DecimalMin(value = "0.0000000001", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
