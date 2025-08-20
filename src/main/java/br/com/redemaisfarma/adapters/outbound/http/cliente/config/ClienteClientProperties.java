package br.com.redemaisfarma.adapters.outbound.http.cliente.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "integrations.cliente")
public class ClienteClientProperties {

    @NotBlank
    private String baseUrl;

    // limite de buffer (bytes) para respostas grandes – default 10 MB
    private int maxInMemorySize = 10 * 1024 * 1024;

    // timeouts específicos (opcional, caso queira usar filtros no client)
    private int readTimeoutMs = 10000;
    private int writeTimeoutMs = 10000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public void setWriteTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }
}
