package com.cryptox.service;

import com.cryptox.dto.BuyRequest;
import com.cryptox.dto.SellRequest;
import com.cryptox.dto.SwapRequest;
import com.cryptox.dto.TradeResponse;
import com.cryptox.exception.ApiException;
import com.cryptox.model.*;
import com.cryptox.repository.TransactionRepository;
import com.cryptox.repository.WalletBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Demo-account trading: buy/sell against the wallet's USDT balance, or swap
 * directly between two coins, all priced from MarketDataService's live
 * CoinGecko feed. This never touches a real exchange or moves real funds —
 * it debits/credits the same internal WalletBalance ledger that SEND/RECEIVE
 * uses, so trade history shows up in the same transaction table.
 */
@Service
@RequiredArgsConstructor
public class TradeService {

    private static final int PRICE_SCALE = 10;

    private final WalletService walletService;
    private final WalletBalanceRepository walletBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataService marketDataService;
    private final CoinRegistry coinRegistry;

    @Transactional
    public TradeResponse buy(String username, BuyRequest request) {
        String coin = request.getCoinSymbol().toUpperCase();
        if (coin.equals("USDT")) {
            throw new ApiException("Cannot buy USDT with USDT", HttpStatus.BAD_REQUEST);
        }
        assertSupported(coin);

        Wallet wallet = walletService.getWalletByOwnerUsername(username);
        WalletBalance usdtBalance = requireBalance(wallet, "USDT");

        if (usdtBalance.getAmount().compareTo(request.getUsdtAmount()) < 0) {
            throw new ApiException("Insufficient USDT balance", HttpStatus.BAD_REQUEST);
        }

        BigDecimal price = marketDataService.getLiveUsdPrice(coinRegistry.coinGeckoId(coin));
        BigDecimal coinAmount = request.getUsdtAmount().divide(price, PRICE_SCALE, RoundingMode.DOWN);

        debit(usdtBalance, request.getUsdtAmount());
        credit(wallet, coin, coinAmount);

        Transaction tx = recordTrade(wallet, TransactionType.BUY, "USDT", request.getUsdtAmount(), coin, coinAmount, price);

        String shareText = String.format("Just bought %s %s for %s USDT on CryptoX \uD83D\uDE80",
                stripTrailingZeros(coinAmount), coin, stripTrailingZeros(request.getUsdtAmount()));

        return new TradeResponse(tx.getId(), TransactionType.BUY, "USDT", request.getUsdtAmount(),
                coin, coinAmount, price, tx.getTimestamp(), shareText);
    }

    @Transactional
    public TradeResponse sell(String username, SellRequest request) {
        String coin = request.getCoinSymbol().toUpperCase();
        if (coin.equals("USDT")) {
            throw new ApiException("Cannot sell USDT for USDT", HttpStatus.BAD_REQUEST);
        }
        assertSupported(coin);

        Wallet wallet = walletService.getWalletByOwnerUsername(username);
        WalletBalance coinBalance = requireBalance(wallet, coin);

        if (coinBalance.getAmount().compareTo(request.getCoinAmount()) < 0) {
            throw new ApiException("Insufficient " + coin + " balance", HttpStatus.BAD_REQUEST);
        }

        BigDecimal price = marketDataService.getLiveUsdPrice(coinRegistry.coinGeckoId(coin));
        BigDecimal usdtAmount = request.getCoinAmount().multiply(price, new MathContext(PRICE_SCALE + 5))
                .setScale(PRICE_SCALE, RoundingMode.DOWN);

        debit(coinBalance, request.getCoinAmount());
        credit(wallet, "USDT", usdtAmount);

        Transaction tx = recordTrade(wallet, TransactionType.SELL, coin, request.getCoinAmount(), "USDT", usdtAmount, price);

        String shareText = String.format("Just sold %s %s for %s USDT on CryptoX \uD83D\uDCB0",
                stripTrailingZeros(request.getCoinAmount()), coin, stripTrailingZeros(usdtAmount));

        return new TradeResponse(tx.getId(), TransactionType.SELL, coin, request.getCoinAmount(),
                "USDT", usdtAmount, price, tx.getTimestamp(), shareText);
    }

    @Transactional
    public TradeResponse swap(String username, SwapRequest request) {
        String fromCoin = request.getFromCoinSymbol().toUpperCase();
        String toCoin = request.getToCoinSymbol().toUpperCase();

        if (fromCoin.equals(toCoin)) {
            throw new ApiException("Cannot swap a coin into itself", HttpStatus.BAD_REQUEST);
        }
        assertSupported(fromCoin);
        assertSupported(toCoin);

        Wallet wallet = walletService.getWalletByOwnerUsername(username);
        WalletBalance fromBalance = requireBalance(wallet, fromCoin);

        if (fromBalance.getAmount().compareTo(request.getAmount()) < 0) {
            throw new ApiException("Insufficient " + fromCoin + " balance", HttpStatus.BAD_REQUEST);
        }

        BigDecimal fromPrice = marketDataService.getLiveUsdPrice(coinRegistry.coinGeckoId(fromCoin));
        BigDecimal toPrice = marketDataService.getLiveUsdPrice(coinRegistry.coinGeckoId(toCoin));

        BigDecimal usdValue = request.getAmount().multiply(fromPrice, new MathContext(PRICE_SCALE + 5));
        BigDecimal toAmount = usdValue.divide(toPrice, PRICE_SCALE, RoundingMode.DOWN);

        debit(fromBalance, request.getAmount());
        credit(wallet, toCoin, toAmount);

        // Effective cross rate, useful to display/share alongside the two USD prices.
        BigDecimal effectiveRate = fromPrice.divide(toPrice, PRICE_SCALE, RoundingMode.DOWN);

        Transaction tx = recordTrade(wallet, TransactionType.SWAP, fromCoin, request.getAmount(), toCoin, toAmount, effectiveRate);

        String shareText = String.format("Just swapped %s %s \u2192 %s %s on CryptoX \uD83D\uDD04",
                stripTrailingZeros(request.getAmount()), fromCoin, stripTrailingZeros(toAmount), toCoin);

        return new TradeResponse(tx.getId(), TransactionType.SWAP, fromCoin, request.getAmount(),
                toCoin, toAmount, effectiveRate, tx.getTimestamp(), shareText);
    }

    // ---- helpers ----

    private void assertSupported(String coin) {
        if (!coinRegistry.isSupported(coin)) {
            throw new ApiException("Unsupported coin: " + coin, HttpStatus.BAD_REQUEST);
        }
    }

    private WalletBalance requireBalance(Wallet wallet, String coin) {
        return walletBalanceRepository.findByWalletAndCoinSymbol(wallet, coin)
                .orElseThrow(() -> new ApiException("You have no " + coin + " balance", HttpStatus.BAD_REQUEST));
    }

    private void debit(WalletBalance balance, BigDecimal amount) {
        balance.setAmount(balance.getAmount().subtract(amount));
        walletBalanceRepository.save(balance);
    }

    private void credit(Wallet wallet, String coin, BigDecimal amount) {
        WalletBalance balance = walletBalanceRepository.findByWalletAndCoinSymbol(wallet, coin)
                .orElseGet(() -> WalletBalance.builder().wallet(wallet).coinSymbol(coin).amount(BigDecimal.ZERO).build());
        balance.setAmount(balance.getAmount().add(amount));
        walletBalanceRepository.save(balance);
    }

    private Transaction recordTrade(Wallet wallet, TransactionType type, String fromCoin, BigDecimal fromAmount,
                                     String toCoin, BigDecimal toAmount, BigDecimal executionPrice) {
        Transaction tx = Transaction.builder()
                .fromWalletId(wallet.getWalletId())
                .toWalletId(wallet.getWalletId()) // trades settle within the same wallet, unlike SEND
                .coinSymbol(fromCoin)
                .amount(fromAmount)
                .counterCoinSymbol(toCoin)
                .counterAmount(toAmount)
                .executionPriceUsd(executionPrice)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .build();
        return transactionRepository.save(tx);
    }

    private String stripTrailingZeros(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
