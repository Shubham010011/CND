package com.cryptox.repository;

import com.cryptox.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByWalletId(String walletId);
    Optional<Wallet> findByOwner_Username(String username);
    boolean existsByWalletId(String walletId);
}
