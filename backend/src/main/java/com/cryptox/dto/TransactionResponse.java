package com.cryptox.dto;

import com.cryptox.model.TransactionStatus;
import com.cryptox.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String fromWalletId;
    private String toWalletId;
    private String coinSymbol;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String note;
    private Instant timestamp;
    /** Only present for BUY/SELL/SWAP rows. */
    private String counterCoinSymbol;
    private BigDecimal counterAmount;
    private BigDecimal executionPriceUsd;
}
