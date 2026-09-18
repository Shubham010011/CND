package com.cryptox.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Buy `coinSymbol` by spending `usdtAmount` of your USDT balance, at the live market price. */
@Data
public class BuyRequest {
    @NotBlank
    private String coinSymbol;

    @NotNull
    @DecimalMin(value = "0.01", message = "Minimum spend is 0.01 USDT")
    private BigDecimal usdtAmount;
}
