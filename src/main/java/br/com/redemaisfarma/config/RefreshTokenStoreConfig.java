// src/main/java/br/com/redemaisfarma/config/RefreshTokenStoreConfig.java
package br.com.redemaisfarma.config;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenJpaStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RefreshTokenStoreConfig {

    @Bean
    @Primary
    public RefreshTokenStore refreshTokenStore(RefreshTokenJpaStore jpaStore) {
        return jpaStore; // usa JPA por padrão
    }
}
