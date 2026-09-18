package com.cryptox.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Pulls LIVE crypto market data from CoinGecko's free, keyless public API.
 * https://www.coingecko.com/en/api/documentation
 *
 * Note: the free tier is rate-limited (roughly 10-30 calls/min per IP).
 * For heavier traffic, add short-lived server-side caching (e.g. Caffeine)
 * in front of these calls, or move to CoinGecko's paid Pro plan / another
 * provider (e.g. Binance public REST API) — the WebClient wiring here
 * would stay nearly identical.
 */
@Service
public class MarketDataService {

    private final WebClient webClient;
    private final String defaultCoins;
    private final String appkey;

    public MarketDataService(WebClient marketDataWebClient,
                              @Value("${app.market.default-coins}") String defaultCoins,
                               @Value("${app.market.api-key}")String appkey)  {
        this.webClient = marketDataWebClient;
        this.defaultCoins = defaultCoins;
        this.appkey=appkey;
    }

    /** Live simple prices (USD) for a comma-separated list of CoinGecko coin IDs, e.g. "bitcoin,ethereum". */
    public Mono<Map> getSimplePrices(String coinIdsCsv, String vsCurrenciesCsv) {
        String ids = (coinIdsCsv == null || coinIdsCsv.isBlank()) ? defaultCoins : coinIdsCsv;
        String vs = (vsCurrenciesCsv == null || vsCurrenciesCsv.isBlank()) ? "usd" : vsCurrenciesCsv;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/simple/price")
                        .queryParam("ids", ids)
                        .queryParam("vs_currencies", vs)
                        .queryParam("include_24hr_change", "true")
                        .build())
                .retrieve()
                .bodyToMono(Map.class);
    }

    /** Richer market listing: rank, price, market cap, 24h change, volume, etc. */
    public Mono<Object[]> getMarkets(String vsCurrency, int perPage, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/coins/markets")
                        .queryParam("vs_currency", vsCurrency == null ? "usd" : vsCurrency)
                        .queryParam("order", "market_cap_desc")
                        .queryParam("per_page", perPage)
                        .queryParam("page", page)
                        .queryParam("sparkline", "false")
                        .queryParam("price_change_percentage", "24h")
                        .build())
                .retrieve()
                .bodyToMono(Object[].class);
    }

    /**
     * Blocking helper that returns a single coin's live USD price as a plain
     * BigDecimal, for use inside synchronous, transactional trade logic
     * (TradeService). Blocking a reactive WebClient call like this is fine at
     * demo scale but would need rethinking (e.g. an async trade-execution
     * flow, or a cached price feed) under real production load.
     */
    @SuppressWarnings("unchecked")
    public java.math.BigDecimal getLiveUsdPrice(String coinGeckoId) {
        Map result = getSimplePrices(coinGeckoId, "usd").block();
        if (result == null || !result.containsKey(coinGeckoId)) {
            throw new IllegalStateException("Could not fetch live price for " + coinGeckoId);
        }
        Map<String, Object> priceInfo = (Map<String, Object>) result.get(coinGeckoId);
        Object usd = priceInfo.get("usd");
        if (usd == null) {
            throw new IllegalStateException("No USD price returned for " + coinGeckoId);
        }
        return new java.math.BigDecimal(usd.toString());
    }

    /** Full detail + live price for a single coin, e.g. "bitcoin". */
    public Mono<Map> getCoinDetail(String coinId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/coins/{id}")
                        .queryParam("localization", "false")
                        .queryParam("tickers", "false")
                        .queryParam("market_data", "true")
                        .build(coinId))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
