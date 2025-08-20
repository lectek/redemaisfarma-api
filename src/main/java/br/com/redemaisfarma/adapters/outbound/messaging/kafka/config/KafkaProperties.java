package br.com.redemaisfarma.adapters.outbound.messaging.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    /** Lista de brokers (ex.: "localhost:9092,localhost:9093"). */
    @NotBlank
    private String bootstrapServers;

    /** Cliente id base para produtores (um sufixo randômico será adicionado). */
    @NotBlank
    private String clientId = "redemaisfarma-api";

    /** Número de tentativas do produtor. */
    @NotNull
    @Min(0)
    private Integer retries = 5;

    /** Tamanho do batch (bytes). */
    @NotNull
    @Min(16384)
    private Integer batchSize = 32_768;

    /** Linger (ms) para agrupar mensagens. */
    @NotNull
    @Min(0)
    private Integer lingerMs = 5;

    /** Buffer total do produtor (bytes). */
    @NotNull
    @Min(33_554_432)
    private Integer bufferMemory = 67_108_864;

    /** Habilita idempotência do produtor. */
    private boolean idempotence = true;

    /** Acks (all, 1, 0). Recomenda-se "all" com idempotência. */
    @NotBlank
    private String acks = "all";

    /** Propriedades extras opcionais que serão “pass-through” para o Kafka. */
    private Map<String, String> extra;

    // getters/setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(Integer lingerMs) {
        this.lingerMs = lingerMs;
    }

    public Integer getBufferMemory() {
        return bufferMemory;
    }

    public void setBufferMemory(Integer bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public boolean isIdempotence() {
        return idempotence;
    }

    public void setIdempotence(boolean idempotence) {
        this.idempotence = idempotence;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }
}
