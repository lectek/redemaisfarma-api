// src/main/java/br/com/redemaisfarma/config/JpaBuilderConfig.java
package br.com.redemaisfarma.config;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;

@Configuration
public class JpaBuilderConfig {

  @Bean
  public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
      ObjectProvider<PersistenceUnitManager> pumProvider) {

    var vendor = new HibernateJpaVendorAdapter();
    // opcional: vendor.setShowSql(false); vendor.setGenerateDdl(false);

    return new EntityManagerFactoryBuilder(
        vendor,
        new HashMap<>(),
        pumProvider.getIfAvailable()
    );
  }
}
