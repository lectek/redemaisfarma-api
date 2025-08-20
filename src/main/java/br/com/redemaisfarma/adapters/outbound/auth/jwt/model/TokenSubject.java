package br.com.redemaisfarma.adapters.outbound.auth.jwt.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Payload “de domínio” que vira o subject/claims do Access Token. Evite colocar dados sensíveis aqui.
 */
public class TokenSubject implements Serializable {
    private Long userId;
    private String username;
    private String tenantId;
    private List<String> roles;

    public TokenSubject() {
    }

    public TokenSubject(Long userId, String username, String tenantId, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.tenantId = tenantId;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TokenSubject))
            return false;
        TokenSubject that = (TokenSubject) o;
        return Objects.equals(userId, that.userId) && Objects.equals(username, that.username)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, tenantId, roles);
    }
}
