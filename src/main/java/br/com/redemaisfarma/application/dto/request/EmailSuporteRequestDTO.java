/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="EmailSuporteRequestDTO", description="Dados para envio de e-mail ao suporte t\u00e9cnico")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class EmailSuporteRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico da solicita\u00e7\u00e3o de suporte", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="solicitacaoId")
    private UUID solicitacaoId;
    @Schema(description="Nome completo do cliente", example="Maria Oliveira", required=true)
    @NotBlank(message="{emailSuporte.nome.notBlank}")
    @Size(min=5, max=100, message="{emailSuporte.nome.size}")
    @Pattern(regexp=".*\\s+.*", message="{emailSuporte.nome.nomeCompleto}")
    @JsonProperty(value="nome")
    private @NotBlank(message="{emailSuporte.nome.notBlank}") @Size(min=5, max=100, message="{emailSuporte.nome.size}") @Pattern(regexp=".*\\s+.*", message="{emailSuporte.nome.nomeCompleto}") String nome;
    @Schema(description="E-mail do cliente", example="maria@exemplo.com", required=true)
    @NotBlank(message="{emailSuporte.email.notBlank}")
    @Email(message="{emailSuporte.email.valid}")
    @Size(max=150, message="{emailSuporte.email.size}")
    @JsonProperty(value="email")
    private @NotBlank(message="{emailSuporte.email.notBlank}") @Email(message="{emailSuporte.email.valid}") @Size(max=150, message="{emailSuporte.email.size}") String email;
    @Schema(description="Assunto da mensagem", example="Erro no pedido de medicamento", required=true)
    @NotBlank(message="{emailSuporte.assunto.notBlank}")
    @Size(max=150, message="{emailSuporte.assunto.size}")
    @JsonProperty(value="assunto")
    private @NotBlank(message="{emailSuporte.assunto.notBlank}") @Size(max=150, message="{emailSuporte.assunto.size}") String assunto;
    @Schema(description="Categoria de suporte", example="PEDIDO", required=true, allowableValues={"PEDIDO", "TECNICO", "FINANCEIRO", "OUTROS"})
    @NotNull(message="{emailSuporte.categoria.notNull}")
    @JsonProperty(value="categoria")
    private @NotNull(message="{emailSuporte.categoria.notNull}") Categoria categoria;
    @Schema(description="Prioridade da solicita\u00e7\u00e3o", example="HIGH", required=true, allowableValues={"LOW", "MEDIUM", "HIGH"})
    @NotNull(message="{emailSuporte.prioridade.notNull}")
    @JsonProperty(value="prioridade")
    private @NotNull(message="{emailSuporte.prioridade.notNull}") Priority prioridade;
    @Schema(description="Conte\u00fado da mensagem de suporte", example="Ao tentar finalizar o pedido, recebo erro 500.", required=true)
    @NotBlank(message="{emailSuporte.mensagem.notBlank}")
    @Size(min=10, max=2000, message="{emailSuporte.mensagem.size}")
    @JsonProperty(value="mensagem")
    private @NotBlank(message="{emailSuporte.mensagem.notBlank}") @Size(min=10, max=2000, message="{emailSuporte.mensagem.size}") String mensagem;
    @Schema(description="Lista de anexos (nomes ou URLs)")
    @Size(max=5, message="{emailSuporte.anexos.size}")
    @Valid
    @JsonProperty(value="anexos")
    private @Size(max=5, message="{emailSuporte.anexos.size}") @Valid List<@NotBlank @Size(max=200) @Pattern(regexp="^(https?://.+|[\\w\\-. ]+\\.[A-Za-z0-9]{2,10})$", message="{emailSuporte.anexos.pattern}") String> anexos;
    @Schema(description="ID do tenant", example="redemaisfarma-001", required=true)
    @NotBlank(message="{emailSuporte.tenantId.notBlank}")
    @Size(max=60, message="{emailSuporte.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{emailSuporte.tenantId.notBlank}") @Size(max=60, message="{emailSuporte.tenantId.size}") String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Data e hora de envio da solicita\u00e7\u00e3o", type="string", format="date-time", example="2025-07-04T14:30:00", required=true)
    @NotNull(message="{emailSuporte.dataEnvio.notNull}")
    @PastOrPresent(message="{emailSuporte.dataEnvio.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEnvio")
    private @NotNull(message="{emailSuporte.dataEnvio.notNull}") @PastOrPresent(message="{emailSuporte.dataEnvio.pastOrPresent}") LocalDateTime dataEnvio;
    @Schema(description="E-mail para c\u00f3pia oculta (BCC)", example="suporte-gestor@lektec.com.br")
    @Email(message="{emailSuporte.bcc.valid}")
    @Size(max=150, message="{emailSuporte.bcc.size}")
    @JsonProperty(value="bcc")
    private @Email(message="{emailSuporte.bcc.valid}") @Size(max=150, message="{emailSuporte.bcc.size}") String bcc;

    public EmailSuporteRequestDTO() {
    }

    public EmailSuporteRequestDTO(UUID solicitacaoId, String nome, String email, String assunto, Categoria categoria, Priority prioridade, String mensagem, List<String> anexos, String tenantId, UUID traceId, LocalDateTime dataEnvio, String bcc) {
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

    public UUID getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public void setSolicitacaoId(UUID solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssunto() {
        return this.assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public Categoria getCategoria() {
        return this.categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Priority getPrioridade() {
        return this.prioridade;
    }

    public void setPrioridade(Priority prioridade) {
        this.prioridade = prioridade;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public List<String> getAnexos() {
        return this.anexos;
    }

    public void setAnexos(List<String> anexos) {
        this.anexos = anexos;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getDataEnvio() {
        return this.dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getBcc() {
        return this.bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailSuporteRequestDTO)) {
            return false;
        }
        EmailSuporteRequestDTO that = (EmailSuporteRequestDTO)o;
        return Objects.equals(this.solicitacaoId, that.solicitacaoId) && Objects.equals(this.nome, that.nome) && Objects.equals(this.email, that.email) && Objects.equals(this.assunto, that.assunto) && this.categoria == that.categoria && this.prioridade == that.prioridade && Objects.equals(this.mensagem, that.mensagem) && Objects.equals(this.anexos, that.anexos) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.dataEnvio, that.dataEnvio) && Objects.equals(this.bcc, that.bcc);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.solicitacaoId, this.nome, this.email, this.assunto, this.categoria, this.prioridade, this.mensagem, this.anexos, this.tenantId, this.traceId, this.dataEnvio, this.bcc});
    }

    public String toString() {
        return "EmailSuporteRequestDTO{solicitacaoId=" + String.valueOf(this.solicitacaoId) + ", nome='" + this.nome + "', email='" + this.email + "', assunto='" + this.assunto + "', categoria=" + String.valueOf((Object)this.categoria) + ", prioridade=" + String.valueOf((Object)this.prioridade) + ", mensagem='" + this.mensagem + "', anexos=" + String.valueOf(this.anexos) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", dataEnvio=" + String.valueOf(this.dataEnvio) + ", bcc='" + this.bcc + "'}";
    }

    public static enum Categoria {
        PEDIDO,
        TECNICO,
        FINANCEIRO,
        OUTROS;

    }

    public static enum Priority {
        LOW,
        MEDIUM,
        HIGH;

    }
}

