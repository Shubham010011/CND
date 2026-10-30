package com.cryptox.service;

import com.cryptox.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for which coins the demo exchange supports, and how
 * our internal symbol (stored on WalletBalance/Transaction) maps to the
 * CoinGecko coin ID used to fetch a live price. Keep this in sync with
 * `app.market.default-coins` in application.properties and with the coin
 * dropdown options on the frontend.
 */
@Component
public class CoinRegistry {

    private static final Map<String, String> SYMBOL_TO_COINGECKO_ID = new LinkedHashMap<>();

    static {
        SYMBOL_TO_COINGECKO_ID.put("BTC", "bitcoin");
        SYMBOL_TO_COINGECKO_ID.put("ETH", "ethereum");
        SYMBOL_TO_COINGECKO_ID.put("USDT", "tether");
        SYMBOL_TO_COINGECKO_ID.put("BNB", "binancecoin");
        SYMBOL_TO_COINGECKO_ID.put("SOL", "solana");
        SYMBOL_TO_COINGECKO_ID.put("XRP", "ripple");
        SYMBOL_TO_COINGECKO_ID.put("DOGE", "dogecoin");
        SYMBOL_TO_COINGECKO_ID.put("ADA", "cardano");
    }

    public String coinGeckoId(String symbol) {
        String id = SYMBOL_TO_COINGECKO_ID.get(symbol.toUpperCase());
        if (id == null) {
            throw new ApiException("Unsupported coin symbol: " + symbol, HttpStatus.BAD_REQUEST);
        }
        return id;
    }

    public boolean isSupported(String symbol) {
        return SYMBOL_TO_COINGECKO_ID.containsKey(symbol.toUpperCase());
    }

    public Map<String, String> allSymbolsToIds() {
        return SYMBOL_TO_COINGECKO_ID;
    }
}
