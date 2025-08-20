package br.com.redemaisfarma.adapters.outbound.auth.jwt.config;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.provider.JwtTokenProvider;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.InMemoryRefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenJpaStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.RefreshTokenJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties props) {
        return new JwtTokenProvider(props);
    }

    @Bean
    @ConditionalOnProperty(name = "security.refresh.store", havingValue = "in-memory")
    public RefreshTokenStore inMemoryStore() {
        return new InMemoryRefreshTokenStore();
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenStore.class)
    @ConditionalOnBean(RefreshTokenJpaRepository.class)
    public RefreshTokenStore jpaStore(RefreshTokenJpaRepository repository) {
        return new RefreshTokenJpaStore(repository);
    }
}
