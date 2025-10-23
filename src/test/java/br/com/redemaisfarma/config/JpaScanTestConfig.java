package br.com.redemaisfarma.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(basePackages = {
    "br.com.redemaisfarma.adapters.outbound.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
    "br.com.redemaisfarma.adapters.outbound.persistence.repository"
})
public class JpaScanTestConfig {}
