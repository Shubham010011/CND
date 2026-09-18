package com.cryptox.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Immutable ledger record of a transfer between two wallets. */
@Entity
@Table(name = "transaction_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String fromWalletId;

    @Column(nullable = false, length = 64)
    private String toWalletId;

    @Column(nullable = false, length = 15)
    private String coinSymbol;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal amount;

    /** Only populated for BUY/SELL/SWAP: the coin+amount received on the other side of the trade. */
    @Column(length = 15)
    private String counterCoinSymbol;

    @Column(precision = 30, scale = 10)
    private BigDecimal counterAmount;

    /** Live USD price used to execute a BUY/SELL/SWAP, captured for the record. */
    @Column(precision = 30, scale = 10)
    private BigDecimal executionPriceUsd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String note;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = Instant.now();
    }
}
