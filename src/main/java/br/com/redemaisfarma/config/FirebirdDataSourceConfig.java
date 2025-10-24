/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zaxxer.hikari.HikariDataSource
 *  jakarta.persistence.EntityManagerFactory
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.boot.jdbc.DataSourceBuilder
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.core.env.Environment
 *  org.springframework.data.jpa.repository.config.EnableJpaRepositories
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.orm.jpa.JpaTransactionManager
 *  org.springframework.orm.jpa.JpaVendorAdapter
 *  org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
 *  org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 *  org.springframework.transaction.PlatformTransactionManager
 */
package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile(value={"legacy"})
@EnableJpaRepositories(basePackages={"br.com.redemaisfarma.adapters.outbound.legacy.repository"}, entityManagerFactoryRef="firebirdEntityManagerFactory", transactionManagerRef="firebirdTransactionManager")
@ConditionalOnProperty(prefix="spring.datasource.firebird", name={"jdbc-url", "url"}, matchIfMissing=false)
public class FirebirdDataSourceConfig {
    @Bean(name={"firebirdDataSource"})
    @ConfigurationProperties(prefix="spring.datasource.firebird")
    public DataSource firebirdDataSource(Environment env) {
        String url;
        HikariDataSource ds = (HikariDataSource)DataSourceBuilder.create().type(HikariDataSource.class).build();
        if (ds.getJdbcUrl() == null && (url = env.getProperty("spring.datasource.firebird.url")) != null && !url.isBlank()) {
            ds.setJdbcUrl(url);
        }
        return ds;
    }

    @Bean(name={"firebirdEntityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean firebirdEntityManagerFactory(@Qualifier(value="firebirdDataSource") DataSource dataSource) {
        HashMap<String, String> jpaProps = new HashMap<String, String>();
        jpaProps.put("hibernate.dialect", "org.hibernate.community.dialect.FirebirdDialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.show_sql", "false");
        jpaProps.put("hibernate.format_sql", "false");
        jpaProps.put("hibernate.jdbc.time_zone", "UTC");
        jpaProps.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setShowSql(false);
        vendor.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(new String[]{"br.com.redemaisfarma.adapters.outbound.legacy.entity"});
        emf.setPersistenceUnitName("firebirdPU");
        emf.setJpaVendorAdapter((JpaVendorAdapter)vendor);
        emf.setJpaPropertyMap(jpaProps);
        return emf;
    }

    @Bean(name={"firebirdTransactionManager"})
    public PlatformTransactionManager firebirdTransactionManager(@Qualifier(value="firebirdEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name={"jdbcTemplateFirebird"})
    public JdbcTemplate jdbcTemplateFirebird(@Qualifier(value="firebirdDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}

