package com.cryptox.dto;

import com.cryptox.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TradeResponse {
    private Long transactionId;
    private TransactionType type;
    private String fromCoinSymbol;
    private BigDecimal fromAmount;
    private String toCoinSymbol;
    private BigDecimal toAmount;
    private BigDecimal executionPriceUsd;
    private Instant timestamp;
    /** A ready-to-share one-line summary, e.g. "Bought 0.0021 BTC for 150 USDT". */
    private String shareText;
}
