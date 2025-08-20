package br.com.redemaisfarma.adapters.outbound.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientCommonConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10)).compress(true);

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).filter(logRequest())
                .filter(logResponse());
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("Response status: " + clientResponse.statusCode());
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}
