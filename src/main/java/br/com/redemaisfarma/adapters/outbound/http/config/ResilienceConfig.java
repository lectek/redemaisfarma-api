package br.com.redemaisfarma.adapters.outbound.http.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom().failureRateThreshold(50).waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10).build();
    }

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(500)).build();
    }
}
