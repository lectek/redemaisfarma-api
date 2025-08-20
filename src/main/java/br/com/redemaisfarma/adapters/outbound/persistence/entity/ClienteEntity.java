package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade Cliente da RedeMaisFarma. - Validações de domínio - Auditoria (created_at/updated_at) - Versionamento
 * otimista
 */
@Entity
@Table(name = "cliente")
@EntityListeners(AuditingEntityListener.class)
public class ClienteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Size(max = 20, message = "O telefone pode ter no máximo 20 caracteres")
    @Column(length = 20)
    private String telefone;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 14, message = "O CPF deve ter entre 11 e 14 caracteres")
    @Column(unique = true, nullable = false, length = 14)
    private String cpf;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 255, message = "A senha deve ter entre 6 e 255 caracteres")
    @Column(nullable = false, length = 255)
    private String senha;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public ClienteEntity() {
    }

    public ClienteEntity(Long id, String nome, String email, String telefone, String cpf, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
    }

    // Getters/Setters
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
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

    // equals/hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteEntity that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClienteEntity{" + "id=" + id + ", nome='" + nome + '\'' + ", email='" + email + '\'' + ", telefone='"
                + telefone + '\'' + ", cpf='" + cpf + '\'' + ", version=" + version + '}';
    }
}
