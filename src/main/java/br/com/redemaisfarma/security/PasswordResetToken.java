// src/main/java/br/com/redemaisfarma/security/PasswordResetToken.java
package br.com.redemaisfarma.security;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, unique = true, length = 100)
  private String token;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean used = false;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  // getters/setters
}
