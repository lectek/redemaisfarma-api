// src/main/java/br/com/redemaisfarma/application/config/JpaAuditingConfig.java
package br.com.redemaisfarma.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // habilita @CreatedDate / @LastModifiedDate
public class JpaAuditingConfig {
}
