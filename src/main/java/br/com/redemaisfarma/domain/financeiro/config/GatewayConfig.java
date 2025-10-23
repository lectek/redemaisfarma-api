package br.com.redemaisfarma.domain.financeiro.config;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Entidade de configuração de gateway de pagamento.
 * Os campos são genéricos o suficiente para suportar múltiplos provedores.
 */
@Entity
@Table(name = "gateway_config",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_gateway_provedor_nome", columnNames = {"provedor", "nome"})
       },
       indexes = {
           @Index(name = "idx_gateway_provedor", columnList = "provedor"),
           @Index(name = "idx_gateway_ativo", columnList = "ativo")
       })
public class GatewayConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome amigável/exibição para admins */
    @Column(nullable = false, length = 120)
    private String nome;

    /** Identificador do provedor (ex.: pagarme, stone, cielo) */
    @Column(nullable = false, length = 60)
    private String provedor;

    @Column(name = "api_key", nullable = false, length = 255)
    private String apiKey;

    @Column(name = "api_secret", length = 255)
    private String apiSecret;

    @Column(name = "webhook_url", length = 512)
    private String webhookUrl;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 10000;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    /**
     * Campo livre para parâmetros específicos do provedor.
     * Pode ser JSON em string. Tamanho grande para flexibilidade.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    // region Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getProvedor() { return provedor; }
    public void setProvedor(String provedor) { this.provedor = provedor; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public OffsetDateTime getAtualizadoEm() { return atualizadoEm; }
    // endregion

    // region equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GatewayConfig)) return false;
        GatewayConfig that = (GatewayConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "GatewayConfig{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", provedor='" + provedor + '\'' +
                ", ativo=" + ativo +
                '}';
    }
    // endregion
}
