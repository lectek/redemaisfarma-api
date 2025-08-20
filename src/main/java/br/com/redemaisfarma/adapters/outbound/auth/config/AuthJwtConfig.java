package br.com.redemaisfarma.adapters.outbound.auth.config;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.service.JwtTokenService;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.InMemoryRefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenJpaStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklistRepository;
import br.com.redemaisfarma.adapters.outbound.auth.service.AuthTokenFacade;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.RefreshTokenJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthJwtConfig {

    // ===== Infra comuns =====

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // ===== RefreshTokenStore (JPA ou memória) =====

    @Bean
    @ConditionalOnBean(RefreshTokenJpaRepository.class)
    public RefreshTokenStore refreshTokenStoreJpa(RefreshTokenJpaRepository repository) {
        return new RefreshTokenJpaStore(repository);
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenStore.class)
    public RefreshTokenStore refreshTokenStoreInMemory() {
        return new InMemoryRefreshTokenStore();
    }

    // ===== Fachada de Auth (orquestra JWT + refresh + blacklist) =====
    // Depende de:
    // - JwtTokenService -> seu provider que assina/valida JWT (já criado em jwt/provider)
    // - RefreshTokenStore -> acima
    // - TokenBlacklistRepository -> vem do módulo cache (Redis recomendado)
    // - JwtProperties -> já habilitado por @EnableConfigurationProperties
    // - Clock -> acima
    @Bean
    @ConditionalOnMissingBean(AuthTokenFacade.class)
    public AuthTokenFacade authTokenFacade(JwtTokenService jwtTokenService, RefreshTokenStore refreshTokenStore,
            TokenBlacklistRepository blacklistRepository, JwtProperties props, Clock clock) {
        return new AuthTokenFacade(jwtTokenService, refreshTokenStore, blacklistRepository, props, clock);
    }
}
