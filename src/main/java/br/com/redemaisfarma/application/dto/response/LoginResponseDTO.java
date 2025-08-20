package br.com.redemaisfarma.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para login na API RedeMaisFarma.
 *
 * Contém tokens de autenticação, informações de perfil, multitenancy e metadados de auditoria.
 */
@Schema(name = "LoginResponseDTO", description = "Dados retornados após login bem-sucedido")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Token de acesso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank(message = "{loginResponse.accessToken.notBlank}")
    @JsonProperty("accessToken")
    private String accessToken;

    @Schema(description = "Token de refresh JWT", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...", required = true)
    @NotBlank(message = "{loginResponse.refreshToken.notBlank}")
    @JsonProperty("refreshToken")
    private String refreshToken;

    @Schema(description = "Tipo de usuário", example = "CLIENTE", required = true, allowableValues = { "CLIENTE",
            "ATENDENTE", "ADMIN" })
    @NotNull(message = "{loginResponse.userType.notNull}")
    @JsonProperty("userType")
    private UserType userType;

    @Schema(description = "ID do usuário autenticado", example = "12345", required = true)
    @NotNull(message = "{loginResponse.userId.notNull}")
    @JsonProperty("userId")
    private Long userId;

    @Schema(description = "Nome completo do usuário", example = "João da Silva", required = true)
    @NotBlank(message = "{loginResponse.fullName.notBlank}")
    @Size(max = 100, message = "{loginResponse.fullName.size}")
    @JsonProperty("fullName")
    private String fullName;

    @Schema(description = "E-mail do usuário", example = "joao@redemaisfarma.com", required = true)
    @NotBlank(message = "{loginResponse.email.notBlank}")
    @Email(message = "{loginResponse.email.valid}")
    @JsonProperty("email")
    private String email;

    @Schema(description = "Lista de permissões do usuário", example = "[\"READ_ORDERS\",\"WRITE_ORDERS\"]", required = true)
    @NotEmpty(message = "{loginResponse.permissions.notEmpty}")
    @JsonProperty("permissions")
    private List<@NotBlank(message = "{loginResponse.permission.notBlank}") String> permissions;

    @Schema(description = "Timestamp de expiração do token de acesso", type = "string", format = "date-time", example = "2025-07-04T15:00:00", required = true)
    @NotNull(message = "{loginResponse.expiresAt.notNull}")
    @Future(message = "{loginResponse.expiresAt.future}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    @Schema(description = "Timestamp do último login", type = "string", format = "date-time", example = "2025-07-03T18:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("lastLoginAt")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Status da conta do usuário", example = "ACTIVE", required = true, allowableValues = {
            "ACTIVE", "SUSPENDED", "INACTIVE" })
    @NotNull(message = "{loginResponse.accountStatus.notNull}")
    @JsonProperty("accountStatus")
    private AccountStatus accountStatus;

    @Schema(description = "Mensagem de boas-vindas", example = "Bem-vindo à RedeMaisFarma, João!")
    @JsonProperty("welcomeMessage")
    private String welcomeMessage;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{loginResponse.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID)", example = "6fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String accessToken, String refreshToken, UserType userType, Long userId, String fullName,
            String email, List<String> permissions, LocalDateTime expiresAt, LocalDateTime lastLoginAt,
            AccountStatus accountStatus, String welcomeMessage, String tenantId, UUID traceId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userType = userType;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.permissions = permissions;
        this.expiresAt = expiresAt;
        this.lastLoginAt = lastLoginAt;
        this.accountStatus = accountStatus;
        this.welcomeMessage = welcomeMessage;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    // ----------------------------------------------------------------------
    // Getters e Setters
    // ----------------------------------------------------------------------

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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
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

    // ----------------------------------------------------------------------
    // Métodos utilitários
    // ----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LoginResponseDTO))
            return false;
        LoginResponseDTO that = (LoginResponseDTO) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(refreshToken, that.refreshToken)
                && userType == that.userType && Objects.equals(userId, that.userId)
                && Objects.equals(fullName, that.fullName) && Objects.equals(email, that.email)
                && Objects.equals(permissions, that.permissions) && Objects.equals(expiresAt, that.expiresAt)
                && Objects.equals(lastLoginAt, that.lastLoginAt) && accountStatus == that.accountStatus
                && Objects.equals(welcomeMessage, that.welcomeMessage) && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, userType, userId, fullName, email, permissions, expiresAt,
                lastLoginAt, accountStatus, welcomeMessage, tenantId, traceId);
    }

    @Override
    public String toString() {
        return "LoginResponseDTO{" + "accessToken='[PROTECTED]'" + ", refreshToken='[PROTECTED]'" + ", userType="
                + userType + ", userId=" + userId + ", fullName='" + fullName + '\'' + ", email='" + email + '\''
                + ", permissions=" + permissions + ", expiresAt=" + expiresAt + ", lastLoginAt=" + lastLoginAt
                + ", accountStatus=" + accountStatus + ", welcomeMessage='" + welcomeMessage + '\'' + ", tenantId='"
                + tenantId + '\'' + ", traceId=" + traceId + '}';
    }

    // ----------------------------------------------------------------------
    // Enums de domínio
    // ----------------------------------------------------------------------

    public enum UserType {
        CLIENTE, ATENDENTE, ADMIN
    }

    public enum AccountStatus {
        ACTIVE, SUSPENDED, INACTIVE
    }
}
