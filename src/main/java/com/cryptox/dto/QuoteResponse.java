package com.cryptox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class QuoteResponse {
    private String coinSymbol;
    private BigDecimal priceUsd;
}
