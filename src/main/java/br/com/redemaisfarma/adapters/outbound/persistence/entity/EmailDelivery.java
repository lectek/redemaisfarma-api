package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "email_delivery",
       indexes = {
           @Index(name = "idx_email_delivery_status_created", columnList = "status,created_at"),
           @Index(name = "idx_email_delivery_destination", columnList = "destination")
       })
public class EmailDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purpose", length = 64, nullable = false)
    private String purpose;              // ex.: OTP_START, REGISTER_CONFIRM

    @Column(name = "destination", length = 254, nullable = false)
    private String destination;

    // ⚠️ REMOVIDO @Lob — JSON deve ser String (UTF-8), não BLOB
    @Column(name = "payload_json", columnDefinition = "json", nullable = false)
    private String payloadJson;          // JSON string com variáveis (subject, html, etc.)

    @Column(name = "provider", length = 32, nullable = false)
    private String provider;             // SMTP / SES / SENDGRID...

    @Column(name = "status", length = 16, nullable = false)
    private String status;               // PENDING / SENT / FAILED

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "message_id", length = 191)
    private String messageId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /* Getters/Setters */
    public Long getId() { return id; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
