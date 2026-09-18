package com.cryptox.repository;

import com.cryptox.model.Wallet;
import com.cryptox.model.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {
    Optional<WalletBalance> findByWalletAndCoinSymbol(Wallet wallet, String coinSymbol);
}
