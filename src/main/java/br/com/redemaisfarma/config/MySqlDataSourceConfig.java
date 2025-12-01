package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Arrays;
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

    private final Environment env;

    public MySqlDataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = {"dataSource", "mysqlDataSource"})
    @Primary
    public DataSource mysqlDataSource() {
        String jdbcUrl = firstNonBlank(
                env.getProperty("spring.datasource.hikari.jdbc-url"),
                env.getProperty("spring.datasource.jdbc-url"),
                env.getProperty("spring.datasource.url"),
                env.getProperty("SPRING_DATASOURCE_HIKARI_JDBC_URL"),
                env.getProperty("SPRING_DATASOURCE_JDBC_URL"),
                env.getProperty("SPRING_DATASOURCE_URL"),
                buildFromMysqlEnv()
        );
        String username = firstNonBlank(
                env.getProperty("spring.datasource.username"),
                env.getProperty("SPRING_DATASOURCE_USERNAME"),
                env.getProperty("MYSQL_USER")
        );
        String password = firstNonBlank(
                env.getProperty("spring.datasource.password"),
                env.getProperty("SPRING_DATASOURCE_PASSWORD"),
                env.getProperty("MYSQL_PASSWORD")
        );

        if (isBlank(jdbcUrl)) {
            String profiles = String.join(",", env.getActiveProfiles());
            throw new IllegalStateException(
                    "MySQL sem jdbcUrl! Defina spring.datasource.url OU spring.datasource.hikari.jdbc-url (profiles ativos: "
                            + (profiles.isEmpty() ? "<none>" : profiles) + ")."
            );
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        if (!isBlank(username)) cfg.setUsername(username);
        if (!isBlank(password)) cfg.setPassword(password);
        cfg.setDriverClassName(firstNonBlank(
                env.getProperty("spring.datasource.driver-class-name"),
                env.getProperty("SPRING_DATASOURCE_DRIVER_CLASS_NAME"),
                "com.mysql.cj.jdbc.Driver"
        ));

        // Ajustes de robustez na inicialização
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(8000);
        cfg.setValidationTimeout(3000);
        cfg.setInitializationFailTimeout(0);   // não falhar se a 1ª conexão der erro (MySQL atrasado)
        cfg.setKeepaliveTime(15000);           // mantém sockets quentes
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
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf
    ) {
        return SharedEntityManagerCreator.createSharedEntityManager(emf);
    }

    private Map<String, Object> jpaProps() {
        Map<String, Object> p = new HashMap<>();
        putIfPresent(p, "hibernate.hbm2ddl.auto",
                firstNonBlank(
                        env.getProperty("spring.jpa.hibernate.ddl-auto"),
                        env.getProperty("SPRING_JPA_HIBERNATE_DDL_AUTO"),
                        env.getProperty("SPRING_JPA_MYSQL_HIBERNATE_DDL_AUTO"),
                        "none"));
        putIfPresent(p, "hibernate.dialect",
                firstNonBlank(
                        env.getProperty("spring.jpa.properties.hibernate.dialect"),
                        env.getProperty("SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"),
                        env.getProperty("SPRING_JPA_MYSQL_PROPERTIES_HIBERNATE_DIALECT")));
        putIfPresent(p, "hibernate.show_sql",
                firstNonBlank(
                        env.getProperty("spring.jpa.show-sql"),
                        env.getProperty("SPRING_JPA_SHOW_SQL"),
                        env.getProperty("SPRING_JPA_MYSQL_SHOW_SQL")));
        putIfPresent(p, "hibernate.format_sql",
                firstNonBlank(
                        env.getProperty("spring.jpa.properties.hibernate.format_sql"),
                        env.getProperty("SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL")));
        putIfPresent(p, "hibernate.jdbc.time_zone",
                firstNonBlank(env.getProperty("spring.jpa.properties.hibernate.jdbc.time_zone"), "UTC"));
        return p;
    }

    private void putIfPresent(Map<String, Object> map, String key, String val) {
        if (!isBlank(val)) map.put(key, val);
    }

    private String buildFromMysqlEnv() {
        String db = env.getProperty("MYSQL_DATABASE");
        if (isBlank(db)) return null;
        return "jdbc:mysql://mysql:3306/" + db
                + "?sslMode=PREFERRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... vals) {
        return Arrays.stream(vals).filter(v -> v != null && !v.trim().isEmpty()).findFirst().orElse(null);
    }
}
