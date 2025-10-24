/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zaxxer.hikari.HikariConfig
 *  com.zaxxer.hikari.HikariDataSource
 *  jakarta.persistence.EntityManagerFactory
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Primary
 *  org.springframework.core.env.Environment
 *  org.springframework.data.jpa.repository.config.EnableJpaRepositories
 *  org.springframework.orm.jpa.JpaTransactionManager
 *  org.springframework.orm.jpa.JpaVendorAdapter
 *  org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
 *  org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 *  org.springframework.transaction.PlatformTransactionManager
 *  org.springframework.transaction.annotation.EnableTransactionManagement
 */
package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods=false)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages={"br.com.redemaisfarma.adapters.outbound.persistence", "br.com.redemaisfarma.user.audit", "br.com.redemaisfarma.domain.financeiro.config"}, entityManagerFactoryRef="mysqlEntityManagerFactory", transactionManagerRef="mysqlTransactionManager")
public class MySqlDataSourceConfig {
    private final Environment env;

    public MySqlDataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean(name={"dataSource"})
    @Primary
    public DataSource mysqlDataSource() {
        String jdbcUrl = MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.datasource.hikari.jdbc-url"), this.env.getProperty("spring.datasource.jdbc-url"), this.env.getProperty("spring.datasource.url"), this.env.getProperty("SPRING_DATASOURCE_HIKARI_JDBC_URL"), this.env.getProperty("SPRING_DATASOURCE_JDBC_URL"), this.env.getProperty("SPRING_DATASOURCE_URL"), this.buildFromMysqlEnv());
        String username = MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.datasource.username"), this.env.getProperty("SPRING_DATASOURCE_USERNAME"), this.env.getProperty("MYSQL_USER"));
        String password = MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.datasource.password"), this.env.getProperty("SPRING_DATASOURCE_PASSWORD"), this.env.getProperty("MYSQL_PASSWORD"));
        if (MySqlDataSourceConfig.isBlank(jdbcUrl)) {
            String profiles = String.join((CharSequence)",", this.env.getActiveProfiles());
            throw new IllegalStateException("MySQL sem jdbcUrl! Defina spring.datasource.url OU spring.datasource.hikari.jdbc-url (profiles ativos: " + (profiles.isEmpty() ? "<none>" : profiles) + ").");
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        if (!MySqlDataSourceConfig.isBlank(username)) {
            cfg.setUsername(username);
        }
        if (!MySqlDataSourceConfig.isBlank(password)) {
            cfg.setPassword(password);
        }
        cfg.setDriverClassName(MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.datasource.driver-class-name"), this.env.getProperty("SPRING_DATASOURCE_DRIVER_CLASS_NAME"), "com.mysql.cj.jdbc.Driver"));
        return new HikariDataSource(cfg);
    }

    @Bean(name={"mysqlEntityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(@Qualifier(value="dataSource") DataSource dataSource) {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(new String[]{"br.com.redemaisfarma"});
        emf.setJpaVendorAdapter((JpaVendorAdapter)vendor);
        emf.setJpaPropertyMap(this.jpaProps());
        emf.setPersistenceUnitName("mysqlPU");
        return emf;
    }

    @Bean(name={"mysqlTransactionManager"})
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(@Qualifier(value="mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    private Map<String, Object> jpaProps() {
        HashMap<String, Object> p = new HashMap<String, Object>();
        this.putIfPresent(p, "hibernate.hbm2ddl.auto", MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.jpa.hibernate.ddl-auto"), this.env.getProperty("SPRING_JPA_HIBERNATE_DDL_AUTO"), this.env.getProperty("SPRING_JPA_MYSQL_HIBERNATE_DDL_AUTO"), "none"));
        this.putIfPresent(p, "hibernate.dialect", MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.jpa.properties.hibernate.dialect"), this.env.getProperty("SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"), this.env.getProperty("SPRING_JPA_MYSQL_PROPERTIES_HIBERNATE_DIALECT")));
        this.putIfPresent(p, "hibernate.show_sql", MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.jpa.show-sql"), this.env.getProperty("SPRING_JPA_SHOW_SQL"), this.env.getProperty("SPRING_JPA_MYSQL_SHOW_SQL")));
        this.putIfPresent(p, "hibernate.format_sql", MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.jpa.properties.hibernate.format_sql"), this.env.getProperty("SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL")));
        this.putIfPresent(p, "hibernate.jdbc.time_zone", MySqlDataSourceConfig.firstNonBlank(this.env.getProperty("spring.jpa.properties.hibernate.jdbc.time_zone"), "UTC"));
        return p;
    }

    private void putIfPresent(Map<String, Object> map, String key, String val) {
        if (!MySqlDataSourceConfig.isBlank(val)) {
            map.put(key, val);
        }
    }

    private String buildFromMysqlEnv() {
        String db = this.env.getProperty("MYSQL_DATABASE");
        if (MySqlDataSourceConfig.isBlank(db)) {
            return null;
        }
        return "jdbc:mysql://mysql:3306/" + db + "?sslMode=PREFERRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String ... vals) {
        return Arrays.stream(vals).filter(v -> v != null && !v.trim().isEmpty()).findFirst().orElse(null);
    }
}

