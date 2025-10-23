package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_image_job")
public class ProductImageJobEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;

    @Column(name="status", nullable=false, length=20)
    private String status;

    @Column(name="result_url", length=512)
    private String resultUrl;

    @Column(name="error_msg", length=512)
    private String errorMsg;

    @Column(name="fingerprint", length=128)
    private String fingerprint;

    @Column(name="created_at", nullable=false, updatable=false,
            columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt;

    @Column(name="updated_at", insertable=false,
            columnDefinition = "timestamp null default null on update current_timestamp")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultUrl() { return resultUrl; }
    public void setResultUrl(String resultUrl) { this.resultUrl = resultUrl; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
