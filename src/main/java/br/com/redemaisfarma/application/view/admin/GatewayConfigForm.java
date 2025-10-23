package br.com.redemaisfarma.application.view.admin;

import br.com.redemaisfarma.domain.financeiro.config.GatewayConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.Objects;

/**
 * Form/DTO usado nas telas admin para criar/editar configurações de gateway.
 * Mantém apenas campos necessários à UI e validações de entrada.
 */
public class GatewayConfigForm implements Serializable {

    private Long id;

    @NotBlank(message = "Nome de exibição é obrigatório")
    @Size(max = 120, message = "Nome pode ter no máximo 120 caracteres")
    private String nome;

    /**
     * Identificador do provedor (ex.: "pagarme", "stone", "cielo").
     * Mantido como string para não travar a UI caso um novo provedor surja.
     */
    @NotBlank(message = "Provedor é obrigatório")
    @Size(max = 60, message = "Provedor pode ter no máximo 60 caracteres")
    private String provedor;

    @NotBlank(message = "API Key é obrigatória")
    @Size(max = 255, message = "API Key pode ter no máximo 255 caracteres")
    private String apiKey;

    @Size(max = 255, message = "API Secret pode ter no máximo 255 caracteres")
    private String apiSecret;

    @URL(message = "Webhook URL inválida")
    @Size(max = 512, message = "Webhook URL pode ter no máximo 512 caracteres")
    private String webhookUrl;

    @NotNull(message = "Campo 'ativo' é obrigatório")
    private Boolean ativo = Boolean.TRUE;

    /** Timeout em milissegundos para chamadas ao provedor */
    @NotNull(message = "Timeout é obrigatório")
    private Integer timeoutMs = 10000;

    /** Tentativas de reenvio em caso de falha transitória */
    @NotNull(message = "Quantidade de tentativas é obrigatória")
    private Integer maxRetries = 3;

    /** Campo livre (JSON/Texto) para parâmetros específicos do provedor */
    @Size(max = 4000, message = "Metadados pode ter no máximo 4000 caracteres")
    private String metadata;

    public GatewayConfigForm() {}

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

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    // endregion

    /** Converte o form em entidade de domínio. */
    public GatewayConfig toEntity() {
        GatewayConfig e = new GatewayConfig();
        e.setId(this.id);
        e.setNome(this.nome);
        e.setProvedor(this.provedor);
        e.setApiKey(this.apiKey);
        e.setApiSecret(this.apiSecret);
        e.setWebhookUrl(this.webhookUrl);
        e.setAtivo(Boolean.TRUE.equals(this.ativo));
        e.setTimeoutMs(this.timeoutMs);
        e.setMaxRetries(this.maxRetries);
        e.setMetadata(this.metadata);
        return e;
    }

    /** Constrói um form a partir da entidade (útil para edição). */
    public static GatewayConfigForm fromEntity(GatewayConfig e) {
        Objects.requireNonNull(e, "Entidade não pode ser nula");
        GatewayConfigForm f = new GatewayConfigForm();
        f.setId(e.getId());
        f.setNome(e.getNome());
        f.setProvedor(e.getProvedor());
        f.setApiKey(e.getApiKey());
        f.setApiSecret(e.getApiSecret());
        f.setWebhookUrl(e.getWebhookUrl());
        f.setAtivo(e.isAtivo());
        f.setTimeoutMs(e.getTimeoutMs());
        f.setMaxRetries(e.getMaxRetries());
        f.setMetadata(e.getMetadata());
        return f;
    }
}
