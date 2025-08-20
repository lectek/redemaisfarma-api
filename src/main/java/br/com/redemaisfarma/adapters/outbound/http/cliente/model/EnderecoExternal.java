package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Endereço retornado pelo serviço externo de clientes. Campos e nomes pensados para casar com JSON comum de APIs
 * brasileiras.
 */
public class EnderecoExternal implements Serializable {

    @JsonProperty("cep")
    private String cep;

    @JsonProperty("logradouro")
    private String logradouro;

    @JsonProperty("numero")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado; // UF

    @JsonProperty("ibge")
    private String ibge;

    @JsonProperty("tipo")
    private String tipo; // RESIDENCIAL | COMERCIAL | OUTRO (livre)

    @JsonProperty("principal")
    private Boolean principal;

    // Getters/Setters
    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIbge() {
        return ibge;
    }

    public void setIbge(String ibge) {
        this.ibge = ibge;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EnderecoExternal))
            return false;
        EnderecoExternal that = (EnderecoExternal) o;
        return Objects.equals(cep, that.cep) && Objects.equals(logradouro, that.logradouro)
                && Objects.equals(numero, that.numero) && Objects.equals(cidade, that.cidade)
                && Objects.equals(estado, that.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cep, logradouro, numero, cidade, estado);
    }
}
