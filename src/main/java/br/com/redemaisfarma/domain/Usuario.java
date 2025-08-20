package br.com.redemaisfarma.domain;

import java.util.List;
import java.util.Objects;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;
    private List<String> roles;
    private boolean clienteVip; // ✅ Novo campo

    public Usuario(Long id, String nome, String email, String cpf, String senha, List<String> roles) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.roles = roles;
    }

    // ✅ Construtor adicional com clienteVip, se precisar
    public Usuario(Long id, String nome, String email, String cpf, String senha, List<String> roles,
            boolean clienteVip) {
        this(id, nome, email, cpf, senha, roles);
        this.clienteVip = clienteVip;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isClienteVip() {
        return clienteVip;
    }

    public void setClienteVip(boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    // equals e hashCode baseados em ID
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Usuario))
            return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
