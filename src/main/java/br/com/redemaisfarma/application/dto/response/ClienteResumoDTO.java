package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Informações resumidas do cliente para uso em listagens, combos e referências cruzadas.
 */
@Schema(name = "ClienteResumoDTO", description = "Informações resumidas do cliente")
public class ClienteResumoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("clienteId")
    private UUID clienteId;

    @Schema(description = "Nome completo do cliente", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("nome")
    private String nome;

    /** Construtor padrão para (de)serialização. */
    public ClienteResumoDTO() {
    }

    /** Construtor completo. */
    public ClienteResumoDTO(UUID clienteId, String nome) {
        this.clienteId = clienteId;
        this.nome = nome;
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

    // utilitários

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteResumoDTO))
            return false;
        ClienteResumoDTO that = (ClienteResumoDTO) o;
        return Objects.equals(clienteId, that.clienteId) && Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, nome);
    }

    @Override
    public String toString() {
        return "ClienteResumoDTO{" + "clienteId=" + clienteId + ", nome='" + nome + '\'' + '}';
    }
}
