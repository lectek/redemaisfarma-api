package br.com.redemaisfarma.adapters.outbound.cache.config;

import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(RedisProperties props) {
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
        cfg.setHostName(props.getHost());
        cfg.setPort(props.getPort());
        cfg.setDatabase(props.getDatabase());
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            cfg.setPassword(RedisPassword.of(props.getPassword()));
        }
        return cfg;
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(RedisProperties props) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder b = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(props.getTimeoutMs()));
        if (props.isSsl()) {
            b.useSsl();
        }
        return b.build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration standalone,
            LettuceClientConfiguration lettuce) {
        return new LettuceConnectionFactory(standalone, lettuce);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Customizer opcional para tunning adicional do Lettuce via Spring Boot. Mantido aqui como extensão futura; sem
     * side effects no momento.
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer noopCustomizer() {
        return builder -> {
            /* noop */ };
    }
}
