/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.user.Role
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EntityListeners
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.ForeignKey
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.JoinTable
 *  jakarta.persistence.ManyToMany
 *  jakarta.persistence.NamedAttributeNode
 *  jakarta.persistence.NamedEntityGraph
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.PreUpdate
 *  jakarta.persistence.Table
 *  jakarta.persistence.Transient
 *  jakarta.persistence.UniqueConstraint
 *  jakarta.persistence.Version
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.Email$List
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotBlank$List
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Size
 *  jakarta.validation.constraints.Size$List
 *  org.springframework.data.annotation.CreatedDate
 *  org.springframework.data.annotation.LastModifiedDate
 *  org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NamedEntityGraph(name="Usuario.roles", attributeNodes={@NamedAttributeNode(value="roles")})
@Entity
@Table(name="usuario", uniqueConstraints={@UniqueConstraint(name="uk_usuario_email", columnNames={"email"}), @UniqueConstraint(name="uk_usuario_cpf", columnNames={"cpf"})}, indexes={@Index(name="idx_usuario_email", columnList="email"), @Index(name="idx_usuario_cpf", columnList="cpf")})
@EntityListeners(value={AuditingEntityListener.class})
public class UsuarioEntity
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank, @NotBlank
    @Size(max=120)
@Size(max=120)
    @Column(nullable=false, length=120)
    private @NotBlank, @NotBlank @Size(max=120)
@Size(max=120) String nome;
    @NotBlank, @NotBlank
    @Email, @Email
    @Size(max=150)
@Size(max=150)
    @Column(nullable=false, length=150, unique=true)
    private @NotBlank, @NotBlank @Email, @Email @Size(max=150)
@Size(max=150) String email;
    @NotBlank, @NotBlank
    @Size(min=11, max=14)
@Size(min=11, max=14)
    @Column(nullable=false, length=14, unique=true)
    private @NotBlank, @NotBlank @Size(min=11, max=14)
@Size(min=11, max=14) String cpf;
    @NotBlank, @NotBlank
    @Size(min=8, max=255)
@Size(min=8, max=255)
    @Column(nullable=false, length=255)
    private @NotBlank, @NotBlank @Size(min=8, max=255)
@Size(min=8, max=255) String senha;
    @ManyToMany(fetch=FetchType.EAGER, targetEntity=Role.class)
    @JoinTable(name="usuario_roles", joinColumns={@JoinColumn(name="usuario_id", foreignKey=@ForeignKey(name="fk_usuario_roles_usuario"))}, inverseJoinColumns={@JoinColumn(name="role_id", foreignKey=@ForeignKey(name="fk_usuario_roles_role"))})
    private Set<Role> roles = new HashSet<Role>();
    @Column(name="ultimo_acesso")
    private LocalDateTime ultimoAcesso;
    @NotNull
    @Column(name="cliente_vip", nullable=false)
    private Boolean clienteVip = Boolean.FALSE;
    @NotNull
    @Column(name="tentativas_falhas", nullable=false)
    private Integer tentativasFalhas = 0;
    @CreatedDate
    @Column(name="created_at", updatable=false, nullable=false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
    @Version
    @Column(name="version", nullable=false)
    private Long version;

    public UsuarioEntity() {
    }

    public UsuarioEntity(Long id, String nome, String email, String cpf, String senha, Set<Role> roles, LocalDateTime ultimoAcesso, Boolean clienteVip, Integer tentativasFalhas, LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.roles = roles != null ? roles : new HashSet();
        this.ultimoAcesso = ultimoAcesso;
        this.clienteVip = clienteVip;
        this.tentativasFalhas = tentativasFalhas;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
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
        this.nome = nome != null ? nome.trim() : null;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = UsuarioEntity.normalizeCpf(cpf);
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet();
    }

    @Transient
    public Set<String> getRoleNames() {
        if (this.roles == null) {
            return Set.of();
        }
        return this.roles.stream().filter(Objects::nonNull).map(Role::getNome).filter(Objects::nonNull).map(String::toUpperCase).collect(Collectors.toCollection(HashSet::new));
    }

    public void addRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<Role>();
            }
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (role != null && this.roles != null) {
            this.roles.remove(role);
        }
    }

    public LocalDateTime getUltimoAcesso() {
        return this.ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public Boolean getClienteVip() {
        return this.clienteVip;
    }

    public void setClienteVip(Boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    public Integer getTentativasFalhas() {
        return this.tentativasFalhas;
    }

    public void setTentativasFalhas(Integer tentativasFalhas) {
        this.tentativasFalhas = tentativasFalhas;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
        if (this.tentativasFalhas == null) {
            this.tentativasFalhas = 0;
        }
        if (this.clienteVip == null) {
            this.clienteVip = Boolean.FALSE;
        }
        if (this.roles == null) {
            this.roles = new HashSet<Role>();
        }
        if (this.nome != null) {
            this.nome = this.nome.trim();
        }
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
        this.cpf = UsuarioEntity.normalizeCpf(this.cpf);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.roles == null) {
            this.roles = new HashSet<Role>();
        }
        if (this.nome != null) {
            this.nome = this.nome.trim();
        }
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
        this.cpf = UsuarioEntity.normalizeCpf(this.cpf);
    }

    private static String normalizeCpf(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UsuarioEntity)) {
            return false;
        }
        UsuarioEntity that = (UsuarioEntity)o;
        return this.id != null && this.id.equals(that.id);
    }

    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    public String toString() {
        return "UsuarioEntity{id=" + String.valueOf(this.id) + ", nome='" + this.nome + "', email='" + this.email + "', cpf='" + this.cpf + "', roles=" + String.valueOf(this.getRoleNames()) + ", clienteVip=" + String.valueOf(this.clienteVip) + ", tentativasFalhas=" + String.valueOf(this.tentativasFalhas) + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ", version=" + String.valueOf(this.version) + "}";
    }
}

