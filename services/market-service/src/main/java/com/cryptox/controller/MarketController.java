package com.cryptox.controller;

import com.cryptox.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/** Public (no auth required) live market data endpoints, same shape as a real exchange ticker feed. */
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketDataService marketDataService;

    @GetMapping("/prices")
    public Mono<Map> prices(@RequestParam(required = false) String coins,
                             @RequestParam(required = false) String vsCurrencies) {
        return marketDataService.getSimplePrices(coins, vsCurrencies);
    }

    @GetMapping("/coins")
    public Mono<Object[]> coins(@RequestParam(defaultValue = "usd") String vsCurrency,
                                 @RequestParam(defaultValue = "50") int perPage,
                                 @RequestParam(defaultValue = "1") int page) {
        return marketDataService.getMarkets(vsCurrency, perPage, page);
    }

    @GetMapping("/coins/{coinId}")
    public Mono<Map> coinDetail(@PathVariable String coinId) {
        return marketDataService.getCoinDetail(coinId);
    }
}
