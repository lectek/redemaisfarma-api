package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de resposta para informações detalhadas de cliente.
 *
 * <p>
 * Inclui identificação, dados pessoais, contato, endereço, segmentação, e metadados para auditoria, multitenancy e
 * integração com outros sistemas.
 * </p>
 */
@Schema(name = "ClienteResponseDTO", description = "Representa os dados detalhados do cliente na API.")
public class ClienteResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único do cliente (UUID).", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("clienteId")
    private UUID clienteId;

    @Schema(description = "Nome completo do cliente.", example = "João da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "CPF do cliente (formato válido).", example = "123.456.789-09", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("cpf")
    private String cpf;

    @Schema(description = "Telefone principal de contato.", example = "+55 (83) 99999-9999")
    @JsonProperty("telefone")
    private String telefone;

    @Schema(description = "Endereço completo.", example = "Rua das Flores, 123, João Pessoa - PB")
    @JsonProperty("endereco")
    private String endereco;

    @Schema(description = "Segmento do cliente.", example = "RETAIL")
    @JsonProperty("grupoCliente")
    private String grupoCliente;

    @Schema(description = "Data e hora do cadastro.", type = "string", format = "date-time", example = "2025-07-04T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Sao_Paulo")
    @JsonProperty("dataCadastro")
    private LocalDateTime dataCadastro;

    @Schema(description = "Data e hora da última atualização.", type = "string", format = "date-time", example = "2025-07-10T09:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Sao_Paulo")
    @JsonProperty("dataAtualizacao")
    private LocalDateTime dataAtualizacao;

    @Schema(description = "Identificador do tenant (multi-inquilino).", example = "redemaisfarma-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento para auditoria (UUID).", example = "4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    public ClienteResponseDTO() {
    }

    public ClienteResponseDTO(UUID clienteId, String nome, String cpf, String telefone, String endereco,
            String grupoCliente, LocalDateTime dataCadastro, LocalDateTime dataAtualizacao, String tenantId,
            UUID traceId) {
        this.clienteId = clienteId;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.grupoCliente = grupoCliente;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getGrupoCliente() {
        return grupoCliente;
    }

    public void setGrupoCliente(String grupoCliente) {
        this.grupoCliente = grupoCliente;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteResponseDTO))
            return false;
        ClienteResponseDTO that = (ClienteResponseDTO) o;
        return Objects.equals(clienteId, that.clienteId) && Objects.equals(nome, that.nome)
                && Objects.equals(cpf, that.cpf) && Objects.equals(telefone, that.telefone)
                && Objects.equals(endereco, that.endereco) && Objects.equals(grupoCliente, that.grupoCliente)
                && Objects.equals(dataCadastro, that.dataCadastro)
                && Objects.equals(dataAtualizacao, that.dataAtualizacao) && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, nome, cpf, telefone, endereco, grupoCliente, dataCadastro, dataAtualizacao,
                tenantId, traceId);
    }

    @Override
    public String toString() {
        return "ClienteResponseDTO{" + "clienteId=" + clienteId + ", nome='" + nome + '\'' + ", cpf='" + cpf + '\''
                + ", telefone='" + telefone + '\'' + ", endereco='" + endereco + '\'' + ", grupoCliente='"
                + grupoCliente + '\'' + ", dataCadastro=" + dataCadastro + ", dataAtualizacao=" + dataAtualizacao
                + ", tenantId='" + tenantId + '\'' + ", traceId=" + traceId + '}';
    }
}
