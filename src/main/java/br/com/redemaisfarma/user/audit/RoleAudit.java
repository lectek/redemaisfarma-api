package br.com.redemaisfarma.user.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "role_audit", indexes = {
    @Index(name = "idx_role_audit_target", columnList = "target_user_id, created_at"),
    @Index(name = "idx_role_audit_actor", columnList = "actor_email, created_at")
})
public class RoleAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "action", length = 20, nullable = false)
  private String action; // GRANT | REVOKE

  @Column(name = "target_user_id", nullable = false)
  private Long targetUserId;

  @Column(name = "role_name", length = 50, nullable = false)
  private String roleName;

  @Column(name = "actor_email", length = 150)
  private String actorEmail;

  @Column(name = "ip", length = 64)
  private String ip;

  @Column(name = "user_agent", length = 255)
  private String userAgent;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }

  // getters/setters
  public Long getId() { return id; }

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public Long getTargetUserId() { return targetUserId; }
  public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

  public String getRoleName() { return roleName; }
  public void setRoleName(String roleName) { this.roleName = roleName; }

  public String getActorEmail() { return actorEmail; }
  public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }

  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  // equals/hashCode (por id)
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoleAudit that = (RoleAudit) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "RoleAudit{" +
        "id=" + id +
        ", action='" + action + '\'' +
        ", targetUserId=" + targetUserId +
        ", roleName='" + roleName + '\'' +
        ", actorEmail='" + actorEmail + '\'' +
        ", ip='" + ip + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}
