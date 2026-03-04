package br.com.redemaisfarma.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Collections;

@Configuration
public class JpaBuilderConfig {

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            ObjectProvider<PersistenceUnitManager> pumProvider) {

        final HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(
                vendor,
                Collections.emptyMap(),
                pumProvider.getIfAvailable()
        );
    }
}
