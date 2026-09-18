package com.cryptox.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Sell `coinAmount` of `coinSymbol` back to USDT at the live market price. */
@Data
public class SellRequest {
    @NotBlank
    private String coinSymbol;

    @NotNull
    @DecimalMin(value = "0.0000000001", message = "Amount must be greater than zero")
    private BigDecimal coinAmount;
}
