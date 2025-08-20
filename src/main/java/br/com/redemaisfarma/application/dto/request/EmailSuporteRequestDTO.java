package br.com.redemaisfarma.application.dto.request;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmailSuporteRequestDTO", description = "Dados para envio de e-mail ao suporte técnico")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailSuporteRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da solicitação de suporte", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("solicitacaoId")
    private UUID solicitacaoId;

    @Schema(description = "Nome completo do cliente", example = "Maria Oliveira", required = true)
    @NotBlank(message = "{emailSuporte.nome.notBlank}")
    @Size(min = 5, max = 100, message = "{emailSuporte.nome.size}")
    @Pattern(regexp = ".*\\s+.*", message = "{emailSuporte.nome.nomeCompleto}") // exige ao menos um espaço
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "E-mail do cliente", example = "maria@exemplo.com", required = true)
    @NotBlank(message = "{emailSuporte.email.notBlank}")
    @Email(message = "{emailSuporte.email.valid}")
    @Size(max = 150, message = "{emailSuporte.email.size}")
    @JsonProperty("email")
    private String email;

    @Schema(description = "Assunto da mensagem", example = "Erro no pedido de medicamento", required = true)
    @NotBlank(message = "{emailSuporte.assunto.notBlank}")
    @Size(max = 150, message = "{emailSuporte.assunto.size}")
    @JsonProperty("assunto")
    private String assunto;

    @Schema(description = "Categoria de suporte", example = "PEDIDO", required = true, allowableValues = { "PEDIDO",
            "TECNICO", "FINANCEIRO", "OUTROS" })
    @NotNull(message = "{emailSuporte.categoria.notNull}")
    @JsonProperty("categoria")
    private Categoria categoria;

    @Schema(description = "Prioridade da solicitação", example = "HIGH", required = true, allowableValues = { "LOW",
            "MEDIUM", "HIGH" })
    @NotNull(message = "{emailSuporte.prioridade.notNull}")
    @JsonProperty("prioridade")
    private Priority prioridade;

    @Schema(description = "Conteúdo da mensagem de suporte", example = "Ao tentar finalizar o pedido, recebo erro 500.", required = true)
    @NotBlank(message = "{emailSuporte.mensagem.notBlank}")
    @Size(min = 10, max = 2000, message = "{emailSuporte.mensagem.size}")
    @JsonProperty("mensagem")
    private String mensagem;

    @Schema(description = "Lista de anexos (nomes ou URLs)")
    @Size(max = 5, message = "{emailSuporte.anexos.size}")
    @Valid
    @JsonProperty("anexos")
    private List<@NotBlank @Size(max = 200) @Pattern(regexp = "^(https?://.+|[\\w\\-. ]+\\.[A-Za-z0-9]{2,10})$", message = "{emailSuporte.anexos.pattern}") String> anexos;

    @Schema(description = "ID do tenant", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{emailSuporte.tenantId.notBlank}")
    @Size(max = 60, message = "{emailSuporte.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Data e hora de envio da solicitação", type = "string", format = "date-time", example = "2025-07-04T14:30:00", required = true)
    @NotNull(message = "{emailSuporte.dataEnvio.notNull}")
    @PastOrPresent(message = "{emailSuporte.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataEnvio")
    private LocalDateTime dataEnvio;

    @Schema(description = "E-mail para cópia oculta (BCC)", example = "suporte-gestor@lektec.com.br")
    @Email(message = "{emailSuporte.bcc.valid}")
    @Size(max = 150, message = "{emailSuporte.bcc.size}")
    @JsonProperty("bcc")
    private String bcc;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------
    public EmailSuporteRequestDTO() {
    }

    public EmailSuporteRequestDTO(UUID solicitacaoId, String nome, String email, String assunto, Categoria categoria,
            Priority prioridade, String mensagem, List<String> anexos, String tenantId, UUID traceId,
            LocalDateTime dataEnvio, String bcc) {
        this.solicitacaoId = solicitacaoId;
        this.nome = nome;
        this.email = email;
        this.assunto = assunto;
        this.categoria = categoria;
        this.prioridade = prioridade;
        this.mensagem = mensagem;
        this.anexos = anexos;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataEnvio = dataEnvio;
        this.bcc = bcc;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------
    public UUID getSolicitacaoId() {
        return solicitacaoId;
    }

    public void setSolicitacaoId(UUID solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Priority getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Priority prioridade) {
        this.prioridade = prioridade;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    // ----------------------------------------------------------------------
    // Utilitários
    // ----------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EmailSuporteRequestDTO that))
            return false;
        return Objects.equals(solicitacaoId, that.solicitacaoId) && Objects.equals(nome, that.nome)
                && Objects.equals(email, that.email) && Objects.equals(assunto, that.assunto)
                && categoria == that.categoria && prioridade == that.prioridade
                && Objects.equals(mensagem, that.mensagem) && Objects.equals(anexos, that.anexos)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(traceId, that.traceId)
                && Objects.equals(dataEnvio, that.dataEnvio) && Objects.equals(bcc, that.bcc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solicitacaoId, nome, email, assunto, categoria, prioridade, mensagem, anexos, tenantId,
                traceId, dataEnvio, bcc);
    }

    @Override
    public String toString() {
        return "EmailSuporteRequestDTO{" + "solicitacaoId=" + solicitacaoId + ", nome='" + nome + '\'' + ", email='"
                + email + '\'' + ", assunto='" + assunto + '\'' + ", categoria=" + categoria + ", prioridade="
                + prioridade + ", mensagem='" + mensagem + '\'' + ", anexos=" + anexos + ", tenantId='" + tenantId
                + '\'' + ", traceId=" + traceId + ", dataEnvio=" + dataEnvio + ", bcc='" + bcc + '\'' + '}';
    }

    // ----------------------------------------------------------------------
    // Enums
    // ----------------------------------------------------------------------
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Categoria {
        PEDIDO, TECNICO, FINANCEIRO, OUTROS
    }
}
