package com.cryptox.service;

import com.cryptox.dto.SendCryptoRequest;
import com.cryptox.dto.TransactionResponse;
import com.cryptox.exception.ApiException;
import com.cryptox.model.*;
import com.cryptox.repository.TransactionRepository;
import com.cryptox.repository.WalletBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final WalletBalanceRepository walletBalanceRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Transfers `amount` of `coinSymbol` from the authenticated user's wallet
     * to the wallet identified by `toWalletId`. Debits and credits happen in
     * one DB transaction so the ledger can never be left half-updated.
     */
    @Transactional
    public TransactionResponse sendCrypto(String senderUsername, SendCryptoRequest request) {
        Wallet fromWallet = walletService.getWalletByOwnerUsername(senderUsername);
        Wallet toWallet = walletService.getWalletByWalletId(request.getToWalletId());

        if (fromWallet.getWalletId().equals(toWallet.getWalletId())) {
            throw new ApiException("Cannot send funds to your own wallet", HttpStatus.BAD_REQUEST);
        }

        String coin = request.getCoinSymbol().toUpperCase();

        WalletBalance senderBalance = walletBalanceRepository
                .findByWalletAndCoinSymbol(fromWallet, coin)
                .orElseThrow(() -> new ApiException("You have no " + coin + " balance", HttpStatus.BAD_REQUEST));

        if (senderBalance.getAmount().compareTo(request.getAmount()) < 0) {
            recordFailedTransaction(fromWallet, toWallet, coin, request.getAmount(), request.getNote());
            throw new ApiException("Insufficient " + coin + " balance", HttpStatus.BAD_REQUEST);
        }

        // Debit sender
        senderBalance.setAmount(senderBalance.getAmount().subtract(request.getAmount()));
        walletBalanceRepository.save(senderBalance);

        // Credit receiver (create the balance row if this is their first time holding this coin)
        WalletBalance receiverBalance = walletBalanceRepository
                .findByWalletAndCoinSymbol(toWallet, coin)
                .orElseGet(() -> WalletBalance.builder()
                        .wallet(toWallet)
                        .coinSymbol(coin)
                        .amount(BigDecimal.ZERO)
                        .build());
        receiverBalance.setAmount(receiverBalance.getAmount().add(request.getAmount()));
        walletBalanceRepository.save(receiverBalance);

        Transaction tx = Transaction.builder()
                .fromWalletId(fromWallet.getWalletId())
                .toWalletId(toWallet.getWalletId())
                .coinSymbol(coin)
                .amount(request.getAmount())
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .note(request.getNote())
                .build();
        tx = transactionRepository.save(tx);

        return toResponse(tx);
    }

    private void recordFailedTransaction(Wallet from, Wallet to, String coin, BigDecimal amount, String note) {
        Transaction tx = Transaction.builder()
                .fromWalletId(from.getWalletId())
                .toWalletId(to.getWalletId())
                .coinSymbol(coin)
                .amount(amount)
                .type(TransactionType.SEND)
                .status(TransactionStatus.FAILED)
                .note(note)
                .build();
        transactionRepository.save(tx);
    }

    public List<TransactionResponse> getHistoryForUser(String username) {
        Wallet wallet = walletService.getWalletByOwnerUsername(username);
        return transactionRepository
                .findByFromWalletIdOrToWalletIdOrderByTimestampDesc(wallet.getWalletId(), wallet.getWalletId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(), tx.getFromWalletId(), tx.getToWalletId(), tx.getCoinSymbol(),
                tx.getAmount(), tx.getType(), tx.getStatus(), tx.getNote(), tx.getTimestamp(),
                tx.getCounterCoinSymbol(), tx.getCounterAmount(), tx.getExecutionPriceUsd()
        );
    }
}
