package com.cryptox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient marketDataWebClient(
        @Value("${app.market.base-url}") String baseUrl,
        @Value ("${app.market.api-key}")  String appkey) {
         WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl);

        if (appkey != null && !appkey.isBlank()) {
            builder.defaultHeader("x-cg-demo-api-key", appkey);
        }

        return builder.build();

    }
}