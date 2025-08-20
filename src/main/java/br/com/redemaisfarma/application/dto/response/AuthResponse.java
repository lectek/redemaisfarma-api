package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name = "AuthResponse", description = "Dados retornados após autenticação")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Token de acesso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("accessToken")
    private String accessToken;

    @Schema(description = "Token de refresh JWT (pode não ser retornado)", example = "dGhpcyBpcyBhIHJlZnJlc2g...")
    @JsonProperty("refreshToken")
    private String refreshToken;

    @Schema(description = "ID do usuário autenticado", example = "42")
    @JsonProperty("userId")
    private Long userId;

    @Schema(description = "Username do usuário", example = "joao.silva")
    @JsonProperty("username")
    private String username;

    @Schema(description = "E-mail do usuário", example = "joao.silva@farmacia.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "Papéis/permissões do usuário", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    @JsonProperty("roles")
    private List<String> roles;

    @Schema(description = "Expiração do accessToken", type = "string", format = "date-time", example = "2025-07-04T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de correlação (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Timestamp de emissão do token", type = "string", format = "date-time", example = "2025-07-04T14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("issuedAt")
    private LocalDateTime issuedAt;

    public AuthResponse() {
    }

    // ===== Construtor compatível com AuthServiceImpl (9 parâmetros, sem email)
    public AuthResponse(String accessToken, String refreshToken, Long userId, String username, List<String> roles,
            LocalDateTime expiresAt, String tenantId, UUID traceId, LocalDateTime issuedAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.issuedAt = issuedAt;
        this.email = null; // opcional
    }

    // ===== Construtor com email (10 parâmetros)
    public AuthResponse(String accessToken, String refreshToken, Long userId, String username, String email,
            List<String> roles, LocalDateTime expiresAt, String tenantId, UUID traceId, LocalDateTime issuedAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.issuedAt = issuedAt;
    }

    // Getters/Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AuthResponse))
            return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(refreshToken, that.refreshToken)
                && Objects.equals(userId, that.userId) && Objects.equals(username, that.username)
                && Objects.equals(email, that.email) && Objects.equals(roles, that.roles)
                && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId) && Objects.equals(issuedAt, that.issuedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, userId, username, email, roles, expiresAt, tenantId, traceId,
                issuedAt);
    }

    @Override
    public String toString() {
        return "AuthResponse{" + "accessToken='[PROTECTED]'" + ", userId=" + userId + ", username='" + username + '\''
                + ", email='" + email + '\'' + ", roles=" + roles + ", expiresAt=" + expiresAt + ", tenantId='"
                + tenantId + '\'' + ", traceId=" + traceId + ", issuedAt=" + issuedAt + '}';
    }
}
