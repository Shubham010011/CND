package com.cryptox.service;

import com.cryptox.dto.WalletBalanceDto;
import com.cryptox.dto.WalletResponse;
import com.cryptox.exception.ApiException;
import com.cryptox.model.User;
import com.cryptox.model.Wallet;
import com.cryptox.model.WalletBalance;
import com.cryptox.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletAddressGenerator addressGenerator;

    @Value("${app.wallet.starter-balance-usdt}")
    private BigDecimal starterBalanceUsdt;

    @Transactional
    public Wallet createWalletForUser(User user) {
        String walletId;
        do {
            walletId = addressGenerator.generate();
        } while (walletRepository.existsByWalletId(walletId));

        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .owner(user)
                .balances(new ArrayList<>())
                .build();

        // Seed with demo funds so the account is immediately usable for send/receive testing.
        WalletBalance starter = WalletBalance.builder()
                .wallet(wallet)
                .coinSymbol("USDT")
                .amount(starterBalanceUsdt)
                .build();
        wallet.getBalances().add(starter);

        user.setWallet(wallet);
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByOwnerUsername(String username) {
        return walletRepository.findByOwner_Username(username)
                .orElseThrow(() -> new ApiException("Wallet not found for user", HttpStatus.NOT_FOUND));
    }

    public Wallet getWalletByWalletId(String walletId) {
        return walletRepository.findByWalletId(walletId)
                .orElseThrow(() -> new ApiException("No wallet found with that wallet ID", HttpStatus.NOT_FOUND));
    }

    public WalletResponse toResponse(Wallet wallet) {
        List<WalletBalanceDto> balances = wallet.getBalances().stream()
                .map(b -> new WalletBalanceDto(b.getCoinSymbol(), b.getAmount()))
                .toList();
        return new WalletResponse(wallet.getWalletId(), balances);
    }
}
