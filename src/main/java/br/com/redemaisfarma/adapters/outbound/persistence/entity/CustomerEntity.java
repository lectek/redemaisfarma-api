package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "customers",
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_customers_email", columnNames = {"email"}),
         @UniqueConstraint(name = "uk_customers_provider_user", columnNames = {"provider", "provider_user_id"})
       },
       indexes = {
         @Index(name = "idx_customers_provider_user", columnList = "provider, provider_user_id")
       })
public class CustomerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nome", nullable = false, length = 120)
  private String nome;

  @Column(name = "email", length = 180, unique = true)
  private String email; // pode ser null quando provedor não retornar

  @Column(name = "avatar_url", length = 512)
  private String avatarUrl;

  @Column(name = "provider", length = 32) // "google" | "facebook"
  private String provider;

  @Column(name = "provider_user_id", length = 128)
  private String providerUserId;

  @Builder.Default
  @Column(name = "email_verificado", nullable = false)
  private boolean emailVerificado = false;

  @Builder.Default
  @Column(name = "ativo", nullable = false)
  private boolean ativo = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
