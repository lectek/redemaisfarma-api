package br.com.redemaisfarma.domain.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "roles",
    uniqueConstraints = @UniqueConstraint(name = "uk_roles_nome", columnNames = "nome")
)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome; // ADMIN, GERENTE, CLIENTE, ...

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    protected Role() {} // JPA

    public Role(String nome) { this.setNome(nome); }

    public Role(String nome, String descricao) {
        this.setNome(nome);
        this.descricao = descricao;
    }

    // ===== FÁBRICA =====
    public static Role of(String nome) {
        return new Role(nome);
    }

    // ===== GET/SET =====
    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null) {
            this.nome = null;
        } else {
            String n = nome.trim();
            this.nome = n.isEmpty() ? null : n.toUpperCase();
        }
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // --- ALIASES para compatibilidade com quem usava "name" ---
    @Transient public String getName() { return nome; }
    public void setName(String name) { this.setNome(name); }

    // ===== COMPAT HELPERS =====
    @Transient
    public boolean equalsIgnoreCase(String other) {
        return this.nome != null && other != null && this.nome.equalsIgnoreCase(other);
    }

    // (alguns pontos antigos podem ter esse typo)
    @Transient
    public boolean equalsIgnoreCaseCase(String other) {
        return equalsIgnoreCase(other);
    }

    // ===== CALLBACKS =====
    @PrePersist @PreUpdate
    void normalize() {
        setNome(this.nome); // garante trim + upper sempre
    }

    // ===== EQUALS/HASH =====
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role other = (Role) o;
        return (id != null && id.equals(other.id)) ||
               (id == null && other.id == null && Objects.equals(nome, other.nome));
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : Objects.hash(nome); }

    @Override public String toString() {
        return "Role{id=" + id + ", nome='" + nome + "'}";
    }
}
