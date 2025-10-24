/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.domain.user;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;
    private Set<String> roles = new HashSet<String>();
    private boolean clienteVip;
    private int tentativasFalhas;
    private Instant ultimoAcesso;

    public Usuario() {
    }

    public Usuario(String nome, String email, String cpf, String senha) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
    }

    public Usuario(Long id, String nome, String email, String cpf, String senha, Set<String> roles, boolean clienteVip) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.setRoles(roles);
        this.clienteVip = clienteVip;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Set<String> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<String>(roles) : new HashSet();
    }

    public boolean isClienteVip() {
        return this.clienteVip;
    }

    public void setClienteVip(boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    public int getTentativasFalhas() {
        return this.tentativasFalhas;
    }

    public void setTentativasFalhas(int tentativasFalhas) {
        this.tentativasFalhas = tentativasFalhas;
    }

    public Instant getUltimoAcesso() {
        return this.ultimoAcesso;
    }

    public void setUltimoAcesso(Instant ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public void addRole(String role) {
        if (role == null || role.isBlank()) {
            return;
        }
        Object r = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        this.roles.add((String)r);
    }

    public void removeRole(String role) {
        if (role == null) {
            return;
        }
        this.roles.removeIf(r -> r.equalsIgnoreCase(role) || r.equalsIgnoreCase("ROLE_" + role));
    }

    public boolean hasRole(String role) {
        if (role == null) {
            return false;
        }
        Object rWith = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String rNo = role.startsWith("ROLE_") ? role.substring(5) : role;
        return this.roles.stream().anyMatch(arg_0 -> Usuario.lambda$hasRole$1((String)rWith, rNo, arg_0));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario)) {
            return false;
        }
        Usuario that = (Usuario)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "Usuario{id=%d, email=%s}".formatted(this.id, this.email);
    }

    private static /* synthetic */ boolean lambda$hasRole$1(String rWith, String rNo, String r) {
        return r.equalsIgnoreCase(rWith) || r.equalsIgnoreCase(rNo);
    }
}

