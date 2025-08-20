package br.com.redemaisfarma.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private String senha;

    // Novos campos
    private LocalDate dataDeNascimento;
    private boolean ativo = true;

    public Cliente() {
    }

    public Cliente(Long id, String nome, String email, String telefone, String cpf, String senha,
            LocalDate dataDeNascimento, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.dataDeNascimento = dataDeNascimento;
        this.ativo = ativo;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public String getSenha() {
        return senha;
    }

    public LocalDate getDataDeNascimento() {
        return dataDeNascimento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setDataDeNascimento(LocalDate dataDeNascimento) {
        this.dataDeNascimento = dataDeNascimento;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void atualizarTelefone(String novoTelefone) {
        if (novoTelefone == null || novoTelefone.length() < 8) {
            throw new IllegalArgumentException("Telefone inválido.");
        }
        this.telefone = novoTelefone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cliente))
            return false;
        Cliente c = (Cliente) o;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
