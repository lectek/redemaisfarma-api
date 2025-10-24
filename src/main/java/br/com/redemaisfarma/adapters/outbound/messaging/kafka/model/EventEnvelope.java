/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonFormat$Shape
 */
package br.com.redemaisfarma.adapters.outbound.messaging.kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class EventEnvelope<T>
implements Serializable {
    private String eventId;
    private String eventType;
    private String source;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant occurredAt;
    private String tenantId;
    private T data;

    public EventEnvelope() {
    }

    public EventEnvelope(String eventType, String source, String tenantId, T data) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.occuredNow();
        this.tenantId = tenantId;
        this.data = data;
    }

    public void occuredNow() {
        this.occurredAt = Instant.now();
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getOccurredAt() {
        return this.occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventEnvelope)) {
            return false;
        }
        EventEnvelope that = (EventEnvelope)o;
        return Objects.equals(this.eventId, that.eventId);
    }

    public int hashCode() {
        return Objects.hash(this.eventId);
    }
}

