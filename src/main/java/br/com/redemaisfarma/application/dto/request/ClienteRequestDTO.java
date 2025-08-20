package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ClienteRequestDTO", description = "Dados para criação/atualização de cliente")
public class ClienteRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente (envie no update; não envie no create)", example = "42")
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Nome completo do cliente", example = "João da Silva", required = true)
    @NotBlank(message = "{cliente.nome.notBlank}")
    @Size(max = 100, message = "{cliente.nome.size}")
    @JsonProperty("nome")
    private String nome;

    @Schema(description = "E-mail do cliente", example = "joao@email.com", required = true)
    @NotBlank(message = "{cliente.email.notBlank}")
    @Email(message = "{cliente.email.email}")
    @Size(max = 150, message = "{cliente.email.size}")
    @JsonProperty("email")
    private String email;

    @Schema(description = "CPF do cliente (com ou sem máscara)", example = "123.456.789-09", required = true)
    @NotBlank(message = "{cliente.cpf.notBlank}")
    @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "{cliente.cpf.pattern}")
    @Size(max = 14, message = "{cliente.cpf.size}")
    @JsonProperty("cpf")
    private String cpf;

    @Schema(description = "Telefone de contato", example = "+55 (83) 99999-9999", required = true)
    @NotBlank(message = "{cliente.telefone.notBlank}")
    @Pattern(regexp = "\\+?\\d{1,3}? ?\\(?\\d{2}\\)? ?\\d{4,5}-?\\d{4}", message = "{cliente.telefone.pattern}")
    @Size(max = 25, message = "{cliente.telefone.size}")
    @JsonProperty("telefone")
    private String telefone;

    @Schema(description = "Endereço completo", example = "Rua das Flores, 123, João Pessoa - PB", required = true)
    @NotBlank(message = "{cliente.endereco.notBlank}")
    @Size(max = 200, message = "{cliente.endereco.size}")
    @JsonProperty("endereco")
    private String endereco;

    @Schema(description = "Grupo de cliente (ex: RETAIL, WHOLESALE)", example = "RETAIL", required = true)
    @NotNull(message = "{cliente.grupo.notNull}")
    @JsonProperty("grupoCliente")
    private br.com.redemaisfarma.domain.enums.GrupoCliente grupoCliente;

    @Schema(description = "Observações adicionais", example = "Prefere atendimento online")
    @Size(max = 500, message = "{cliente.observacao.size}")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "Data e hora do cadastro (servidor)", type = "string", format = "date-time", example = "2025-07-04T18:30:00")
    @PastOrPresent(message = "{cliente.dataCadastro.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataCadastro", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataCadastro;

    // construtores
    public ClienteRequestDTO() {
    }

    public ClienteRequestDTO(Long id, String nome, String email, String cpf, String telefone, String endereco,
            br.com.redemaisfarma.domain.enums.GrupoCliente grupoCliente, String observacao,
            LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.grupoCliente = grupoCliente;
        this.observacao = observacao;
        this.dataCadastro = dataCadastro;
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    } // manter caso reaproveite para update interno

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

    public br.com.redemaisfarma.domain.enums.GrupoCliente getGrupoCliente() {
        return grupoCliente;
    }

    public void setGrupoCliente(br.com.redemaisfarma.domain.enums.GrupoCliente grupoCliente) {
        this.grupoCliente = grupoCliente;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // equals/hashCode/toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteRequestDTO))
            return false;
        ClienteRequestDTO that = (ClienteRequestDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(nome, that.nome) && Objects.equals(email, that.email)
                && Objects.equals(cpf, that.cpf) && Objects.equals(telefone, that.telefone)
                && Objects.equals(endereco, that.endereco) && grupoCliente == that.grupoCliente
                && Objects.equals(observacao, that.observacao) && Objects.equals(dataCadastro, that.dataCadastro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, cpf, telefone, endereco, grupoCliente, observacao, dataCadastro);
    }

    @Override
    public String toString() {
        return "ClienteRequestDTO{" + "id=" + id + ", nome='" + nome + '\'' + ", email='" + email + '\'' + ", cpf='"
                + cpf + '\'' + ", telefone='" + telefone + '\'' + ", endereco='" + endereco + '\'' + ", grupoCliente="
                + grupoCliente + ", observacao='" + observacao + '\'' + ", dataCadastro=" + dataCadastro + '}';
    }
}
