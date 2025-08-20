package br.com.redemaisfarma.adapters.outbound.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    @NotBlank
    private String host = "localhost";

    @Min(1)
    private int port = 6379;

    private String password; // opcional
    private int database = 0;
    private boolean ssl = false;
    private long timeoutMs = 2000;

    /** Prefixo para chaves do sistema (ex.: "rmf:"). */
    @NotBlank
    private String keyPrefix = "rmf:";

    /** Namespace da blacklist (fica: keyPrefix + blacklistNamespace + hash). */
    @NotBlank
    private String blacklistNamespace = "auth:blacklist:";

    // getters/setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getBlacklistNamespace() {
        return blacklistNamespace;
    }

    public void setBlacklistNamespace(String blacklistNamespace) {
        this.blacklistNamespace = blacklistNamespace;
    }
}
