package br.com.redemaisfarma.config;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.InMemoryTokenBlacklist;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Configuration
@Profile("docker") // ativa só no profile docker
public class TokenBlacklistFallbackConfig {

  @Bean(name = "inMemoryTokenBlacklist")
  @Primary // se houver mais de um, este vence
  @ConditionalOnMissingBean(TokenBlacklist.class) // só cria se não existir outro
  public TokenBlacklist inMemoryTokenBlacklist() {
    return new InMemoryTokenBlacklist();
  }
}
