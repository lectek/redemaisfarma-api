/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.AssertTrue$List
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.Min$List
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.NotNull$List
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.validation.annotation.Validated
 */
package br.com.redemaisfarma.adapters.outbound.messaging.kafka.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix="kafka")
public class KafkaProperties {
    private boolean enabled = true;
    private String bootstrapServers;
    private String clientId = "redemaisfarma-api";
    @NotNull, @NotNull
    @Min(value=0L)
@Min(value=0L)
    private @NotNull, @NotNull @Min(value=0L)
@Min(value=0L) Integer retries = 5;
    @NotNull, @NotNull
    @Min(value=16384L)
@Min(value=16384L)
    private @NotNull, @NotNull @Min(value=16384L)
@Min(value=16384L) Integer batchSize = 32768;
    @NotNull, @NotNull
    @Min(value=0L)
@Min(value=0L)
    private @NotNull, @NotNull @Min(value=0L)
@Min(value=0L) Integer lingerMs = 5;
    @NotNull, @NotNull
    @Min(value=0x2000000L)
@Min(value=0x2000000L)
    private @NotNull, @NotNull @Min(value=0x2000000L)
@Min(value=0x2000000L) Integer bufferMemory = 0x4000000;
    private boolean idempotence = true;
    private String acks = "all";
    private Map<String, Object> extra;

    @AssertTrue(message="kafka.bootstrapServers deve ser informado quando kafka.enabled=true")
@AssertTrue(message="kafka.bootstrapServers deve ser informado quando kafka.enabled=true")
    public @AssertTrue(message="kafka.bootstrapServers deve ser informado quando kafka.enabled=true")
@AssertTrue(message="kafka.bootstrapServers deve ser informado quando kafka.enabled=true") boolean isBootstrapServersValid() {
        return !this.enabled || this.bootstrapServers != null && !this.bootstrapServers.isBlank();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getRetries() {
        return this.retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getLingerMs() {
        return this.lingerMs;
    }

    public void setLingerMs(Integer lingerMs) {
        this.lingerMs = lingerMs;
    }

    public Integer getBufferMemory() {
        return this.bufferMemory;
    }

    public void setBufferMemory(Integer bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    public boolean isIdempotence() {
        return this.idempotence;
    }

    public void setIdempotence(boolean idempotence) {
        this.idempotence = idempotence;
    }

    public String getAcks() {
        return this.acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public Map<String, Object> getExtra() {
        return this.extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}

