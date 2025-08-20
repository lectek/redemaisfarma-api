package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um usuário do sistema RedeMaisFarma. Inclui dados de autenticação, auditoria e controle de
 * tentativas de login.
 *
 * <p>
 * Segue padrões de produção para:
 * <ul>
 * <li>Validação de dados com Bean Validation</li>
 * <li>Auditoria automática com {@link CreatedDate} e {@link LastModifiedDate}</li>
 * <li>Controle de concorrência com @Version</li>
 * </ul>
 */
@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
public class UsuarioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 14, message = "O CPF deve ter entre 11 e 14 caracteres")
    @Column(unique = true, nullable = false, length = 14)
    private String cpf;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 255, message = "A senha deve ter entre 6 e 255 caracteres")
    @Column(nullable = false, length = 255)
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "role", nullable = false, length = 50)
    private List<String> roles;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @NotNull(message = "O campo clienteVip não pode ser nulo")
    @Column(name = "cliente_vip", nullable = false)
    private Boolean clienteVip = Boolean.FALSE;

    @NotNull(message = "O campo tentativasFalhas não pode ser nulo")
    @Column(name = "tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Construtor padrão exigido pelo JPA */
    public UsuarioEntity() {
    }

    /** Construtor completo */
    public UsuarioEntity(Long id, String nome, String email, String cpf, String senha, List<String> roles,
            LocalDateTime ultimoAcesso, Boolean clienteVip, Integer tentativasFalhas, LocalDateTime createdAt,
            LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.roles = roles;
        this.ultimoAcesso = ultimoAcesso;
        this.clienteVip = clienteVip;
        this.tentativasFalhas = tentativasFalhas;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    // =======================
    // GETTERS E SETTERS
    // =======================

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

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    public Boolean getClienteVip() {
        return clienteVip;
    }

    public void setClienteVip(Boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    public Integer getTentativasFalhas() {
        return tentativasFalhas;
    }

    public void setTentativasFalhas(Integer tentativasFalhas) {
        this.tentativasFalhas = tentativasFalhas;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // =======================
    // MÉTODOS UTILITÁRIOS
    // =======================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UsuarioEntity that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UsuarioEntity{" + "id=" + id + ", nome='" + nome + '\'' + ", email='" + email + '\'' + ", cpf='" + cpf
                + '\'' + ", clienteVip=" + clienteVip + ", tentativasFalhas=" + tentativasFalhas + ", createdAt="
                + createdAt + ", updatedAt=" + updatedAt + ", version=" + version + '}';
    }
}
