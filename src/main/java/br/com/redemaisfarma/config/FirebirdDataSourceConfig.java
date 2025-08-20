// src/main/java/br/com/redemaisfarma/config/FirebirdDataSourceConfig.java
package br.com.redemaisfarma.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "br.com.redemaisfarma.firebird.domain")
@EnableJpaRepositories(
    basePackages = "br.com.redemaisfarma.firebird.repository",
    entityManagerFactoryRef = "firebirdEntityManagerFactory",
    transactionManagerRef = "firebirdTransactionManager"
)
public class FirebirdDataSourceConfig {

    @Bean(name = "firebirdDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.firebird")
    public DataSource firebirdDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "firebirdEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean firebirdEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("firebirdDataSource") DataSource dataSource) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.community.dialect.FirebirdDialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.show_sql", "false");
        jpaProps.put("hibernate.format_sql", "false");
        jpaProps.put("hibernate.jdbc.time_zone", "UTC");
        jpaProps.put("hibernate.boot.allow_jdbc_metadata_access", "false");

        return builder
                .dataSource(dataSource)
                .packages("br.com.redemaisfarma.firebird.domain")
                .persistenceUnit("firebirdPU")
                .properties(jpaProps)
                .build();
    }

    @Bean(name = "firebirdTransactionManager")
    public PlatformTransactionManager firebirdTransactionManager(
            @Qualifier("firebirdEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
