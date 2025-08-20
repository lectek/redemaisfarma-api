package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo PRÓPRIO do outbound para a resposta do serviço externo de Cliente. Mantenha este contrato desacoplado das DTOs
 * de application.
 */
public class ClienteExternal implements Serializable {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("email")
    private String email;

    @JsonProperty("telefone")
    private String telefone;

    @JsonProperty("dataNascimento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @JsonProperty("ativo")
    private Boolean ativo;

    @JsonProperty("enderecos")
    private List<EnderecoExternal> enderecos;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<EnderecoExternal> getEnderecos() {
        return enderecos;
    }

    public void setEnderecos(List<EnderecoExternal> enderecos) {
        this.enderecos = enderecos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteExternal))
            return false;
        ClienteExternal that = (ClienteExternal) o;
        return Objects.equals(id, that.id) && Objects.equals(cpf, that.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }
}
