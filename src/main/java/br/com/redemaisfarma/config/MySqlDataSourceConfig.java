package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.GitProperties;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    private final GitProperties gitProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlDataSourceConfig.class);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static final String EXPECTED_FORMAT_MESSAGE = "Formato esperado: mysql://usuario:senha@host:porta/dbname (uma linha, sem espacos e terminando com /dbname).";
    private static final List<String> FALLBACK_URL_ENVS = List.of("RAILWAY_MYSQL_URL", "DATABASE_URL");

    public MySqlDataSourceConfig(@Autowired(required = false) GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Bean(name = {"dataSource", "mysqlDataSource"})
    @Primary
    public DataSource mysqlDataSource() {
        logStartupDiagnostics();
        String rawUrl = resolveDatabaseUrl();
        ParsedUrl parsed = parseDatabaseUrl(rawUrl);
        validateExpectedDatabase(parsed.database());
        logResolvedConfiguration(parsed);

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

    private String resolveDatabaseUrl() {
        String mysqlUrl = trimEnv("MYSQL_URL");
        if (!isBlank(mysqlUrl)) {
            return mysqlUrl;
        }

        for (String envKey : FALLBACK_URL_ENVS) {
            String candidate = trimEnv(envKey);
            if (!isBlank(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Nenhuma das variaveis MYSQL_URL, RAILWAY_MYSQL_URL ou DATABASE_URL foi definida. Configure o Railway/MySQL corretamente.");
    }

    private void validateExpectedDatabase(String actualDatabase) {
        String expectedDatabase = System.getenv("MYSQL_DATABASE");
        if (!isBlank(expectedDatabase)) {
            expectedDatabase = expectedDatabase.trim();
            if (!expectedDatabase.equals(actualDatabase)) {
                throw new IllegalStateException(String.format("MYSQL_DATABASE (%s) difere do banco informado na URL (%s).", expectedDatabase, actualDatabase));
            }
        }
    }

    private ParsedUrl parseDatabaseUrl(String rawUrl) {
        if (isBlank(rawUrl)) {
            throw new IllegalStateException("A URL do MySQL esta vazia. " + EXPECTED_FORMAT_MESSAGE);
        }

        String normalized = rawUrl.trim();
        if (hasWhitespace(normalized)) {
            throw new IllegalStateException("A URL do MySQL contem espacos ou quebras de linha. " + EXPECTED_FORMAT_MESSAGE);
        }

        if (normalized.startsWith("jdbc:")) {
            normalized = normalized.substring("jdbc:".length());
        }

        if (!normalized.startsWith("mysql://")) {
            throw new IllegalStateException("A URL do MySQL deve começar com mysql://. " + EXPECTED_FORMAT_MESSAGE);
        }

        ParsedUrl parsed = tryParseWithUri(normalized);
        if (parsed != null) {
            return parsed;
        }

        return parseManually(normalized);
    }

    private ParsedUrl tryParseWithUri(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            String path = uri.getPath();
            String db = (path != null && path.startsWith("/")) ? path.substring(1) : path;
            db = decodeComponent(db);
            if (isBlank(host) || isBlank(db)) {
                return null;
            }
            if (db.contains("/")) {
                throw new IllegalStateException("A URL do MySQL deve terminar com /dbname. " + EXPECTED_FORMAT_MESSAGE);
            }

            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            if (!isBlank(userInfo)) {
                String[] parts = userInfo.split(":", 2);
                username = decodeComponent(parts[0]);
                if (parts.length > 1) {
                    password = decodeComponent(parts[1]);
                }
            }

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db
                    + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            return new ParsedUrl(jdbcUrl, username, password, db, host, port);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private ParsedUrl parseManually(String normalizedUrl) {
        String withoutScheme = normalizedUrl.substring("mysql://".length());
        int atIndex = withoutScheme.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == withoutScheme.length() - 1) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        String userInfo = withoutScheme.substring(0, atIndex);
        String hostPart = withoutScheme.substring(atIndex + 1);

        int slashIndex = hostPart.indexOf('/');
        if (slashIndex <= 0 || slashIndex == hostPart.length() - 1) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        String hostPort = hostPart.substring(0, slashIndex);
        String dbPart = hostPart.substring(slashIndex + 1);
        int queryIndex = dbPart.indexOf('?');
        String dbSegment = queryIndex >= 0 ? dbPart.substring(0, queryIndex) : dbPart;
        String db = decodeComponent(dbSegment);

        String host;
        int port = 3306;
        int portIdx = hostPort.lastIndexOf(':');
        if (portIdx > 0 && portIdx < hostPort.length() - 1) {
            host = hostPort.substring(0, portIdx);
            try {
                port = Integer.parseInt(hostPort.substring(portIdx + 1));
            } catch (NumberFormatException ignored) {
                port = 3306;
            }
        } else {
            host = hostPort;
        }

        String username = null;
        String password = null;
        int userSep = userInfo.indexOf(':');
        if (userSep >= 0) {
            username = decodeComponent(userInfo.substring(0, userSep));
            password = decodeComponent(userInfo.substring(userSep + 1));
        } else if (!isBlank(userInfo)) {
            username = decodeComponent(userInfo);
        }

        if (isBlank(host) || isBlank(db)) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        if (db.contains("/")) {
            throw new IllegalStateException("A URL do MySQL deve terminar com /dbname. " + EXPECTED_FORMAT_MESSAGE);
        }

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return new ParsedUrl(jdbcUrl, username, password, db, host, port);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    private static boolean hasWhitespace(String value) {
        return value != null && WHITESPACE_PATTERN.matcher(value).find();
    }

    private static void logResolvedConfiguration(ParsedUrl parsed) {
        LOGGER.info("MySQL config resolved host={}, port={}, db={}", parsed.host(), parsed.port(), parsed.database());
    }

    private void logStartupDiagnostics() {
        LOGGER.info("Env presence  MYSQL_URL={}, RAILWAY_MYSQL_URL={}, DATABASE_URL={}",
                hasEnvValue("MYSQL_URL"),
                hasEnvValue("RAILWAY_MYSQL_URL"),
                hasEnvValue("DATABASE_URL"));
        String commit = null;
        if (gitProperties != null) {
            commit = gitProperties.getShortCommitId();
            if (isBlank(commit)) {
                commit = gitProperties.getCommitId();
            }
        }
        LOGGER.info("Build commit={}", commit != null ? commit : "<unknown>");
    }

    private static String trimEnv(String key) {
        String value = System.getenv(key);
        return value != null ? value.trim() : null;
    }

    private static boolean hasEnvValue(String key) {
        return !isBlank(trimEnv(key));
    }

    private static String decodeComponent(String component) {
        if (isBlank(component)) {
            return null;
        }
        try {
            return URLDecoder.decode(component, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return component;
        }
    }

    private record ParsedUrl(String jdbcUrl, String username, String password, String database, String host, int port) {}
}
