package com.cryptox.controller;

import com.cryptox.dto.*;
import com.cryptox.service.CoinRegistry;
import com.cryptox.service.MarketDataService;
import com.cryptox.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final MarketDataService marketDataService;
    private final CoinRegistry coinRegistry;

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(Authentication auth, @Valid @RequestBody BuyRequest request) {
        return ResponseEntity.ok(tradeService.buy(auth.getName(), request));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(Authentication auth, @Valid @RequestBody SellRequest request) {
        return ResponseEntity.ok(tradeService.sell(auth.getName(), request));
    }

    @PostMapping("/swap")
    public ResponseEntity<TradeResponse> swap(Authentication auth, @Valid @RequestBody SwapRequest request) {
        return ResponseEntity.ok(tradeService.swap(auth.getName(), request));
    }

    /** Live quote for one coin in USD, so the UI can show an estimate before the user confirms a trade. */
    @GetMapping("/quote/{coinSymbol}")
    public ResponseEntity<QuoteResponse> quote(@PathVariable String coinSymbol) {
        String coin = coinSymbol.toUpperCase();
        var price = marketDataService.getLiveUsdPrice(coinRegistry.coinGeckoId(coin));
        return ResponseEntity.ok(new QuoteResponse(coin, price));
    }

    /** Which coin symbols this demo exchange supports trading. */
    @GetMapping("/supported-coins")
    public ResponseEntity<Set<String>> supportedCoins() {
        return ResponseEntity.ok(coinRegistry.allSymbolsToIds().keySet());
    }
}
