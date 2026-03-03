package br.com.redemaisfarma.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Map;

@Configuration
public class JpaBuilderConfig {

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            ObjectProvider<PersistenceUnitManager> pumProvider) {

        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(vendor, dataSource -> Map.of(), pumProvider.getIfAvailable());
    }
}
