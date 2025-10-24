/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.reactive.function.client.ExchangeFilterFunction
 *  org.springframework.web.reactive.function.client.WebClient$Builder
 */
package br.com.redemaisfarma.adapters.outbound.http.config;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientCommonConfig {
    private static final Logger log;

    public WebClientCommonConfig() {
        throw new Error("Unresolved compilation problems: \n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientRequest>\n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientResponse>\n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientResponse>\n");
    }

    @Bean
    @ConditionalOnMissingBean(value={WebClient.Builder.class})
    public WebClient.Builder webClientBuilder(@Value(value="${app.http.base-url:}") String string) {
        throw new Error("Unresolved compilation problem: \n");
    }

    private ExchangeFilterFunction logRequest() {
        throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientRequest>\n");
    }

    private ExchangeFilterFunction logResponse() {
        throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientResponse>\n");
    }

    private ExchangeFilterFunction mapErrors() {
        throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from Mono<Object> to Mono<ClientResponse>\n");
    }
}

