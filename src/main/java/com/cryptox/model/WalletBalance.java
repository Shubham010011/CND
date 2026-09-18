package com.cryptox.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Per-coin balance held inside a wallet, e.g. (BTC, 0.0421). */
@Entity
@Table(name = "wallet_balance", uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "coinSymbol"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, length = 15)
    private String coinSymbol; // e.g. BTC, ETH, USDT

    @Column(nullable = false, precision = 30, scale = 10)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
}
