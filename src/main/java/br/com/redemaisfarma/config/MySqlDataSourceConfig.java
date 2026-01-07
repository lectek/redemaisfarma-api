package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Primary
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "br.com.redemaisfarma.adapters.outbound.persistence",
                "br.com.redemaisfarma.user.audit",
                "br.com.redemaisfarma.domain.financeiro.config",
                "br.com.redemaisfarma.domain.user"
        },
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MySqlDataSourceConfig {

    // Railway provides MYSQL_URL automatically; use it as the single source of truth.
    private static final String MYSQL_URL = System.getenv("MYSQL_URL");

    @Bean(name = {"dataSource", "mysqlDataSource"})
    @Primary
    public DataSource mysqlDataSource() {
        if (isBlank(MYSQL_URL)) {
            throw new IllegalStateException(
                    "MYSQL_URL nao definida. Configure a variavel no Railway."
            );
        }

        ParsedUrl parsed = parseRailwayUrl(MYSQL_URL);

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(parsed.jdbcUrl());
        cfg.setUsername(parsed.username());
        cfg.setPassword(parsed.password());
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Startup-friendly defaults for Railway.
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(8000);
        cfg.setValidationTimeout(3000);
        cfg.setInitializationFailTimeout(0);
        cfg.setKeepaliveTime(15000);
        cfg.setIdleTimeout(60000);
        cfg.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(cfg);
    }

    @Bean(name = {"mysqlEntityManagerFactory", "entityManagerFactory"})
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            @Qualifier("dataSource") DataSource dataSource) {

        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(vendor);
        emf.setJpaPropertyMap(jpaProps());
        emf.setPackagesToScan("br.com.redemaisfarma");
        emf.setPersistenceUnitName("mysqlPU");
        return emf;
    }

    @Bean(name = {"mysqlTransactionManager", "transactionManager"})
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "jpaSharedEM_mysqlEntityManagerFactory")
    @Primary
    public EntityManager mysqlSharedEntityManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return SharedEntityManagerCreator.createSharedEntityManager(emf);
    }

    private Map<String, Object> jpaProps() {
        Map<String, Object> p = new HashMap<>();
        p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        p.put("hibernate.hbm2ddl.auto", "none");
        p.put("hibernate.show_sql", "false");
        p.put("hibernate.format_sql", "true");
        p.put("hibernate.jdbc.time_zone", "UTC");
        return p;
    }

    private ParsedUrl parseRailwayUrl(String rawUrl) {
        URI uri = URI.create(rawUrl);
        if (!"mysql".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("MYSQL_URL invalida (esperado mysql://).");
        }

        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 3306;
        String path = uri.getPath();
        String db = (path != null && path.startsWith("/")) ? path.substring(1) : path;

        if (isBlank(host) || isBlank(db)) {
            throw new IllegalStateException("MYSQL_URL invalida (host/db ausentes).");
        }

        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (!isBlank(userInfo)) {
            String[] parts = userInfo.split(":", 2);
            username = parts[0];
            if (parts.length > 1) {
                password = parts[1];
            }
        }

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        return new ParsedUrl(jdbcUrl, username, password);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private record ParsedUrl(String jdbcUrl, String username, String password) {}
}
