/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.ObjectProvider
 *  org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.orm.jpa.JpaVendorAdapter
 *  org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager
 *  org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 */
package br.com.redemaisfarma.config;

import java.util.HashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class JpaBuilderConfig {
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(ObjectProvider<PersistenceUnitManager> pumProvider) {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        return new EntityManagerFactoryBuilder((JpaVendorAdapter)vendor, new HashMap(), (PersistenceUnitManager)pumProvider.getIfAvailable());
    }
}

