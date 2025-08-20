package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de requisição para login de usuários na API RedeMaisFarma. - Suporta e-mail OU CPF como identificador - Campos
 * sensíveis (senha) somente escrita - Campos de auditoria gerenciados pelo servidor (somente leitura)
 */
@Schema(name = "LoginRequest", description = "Dados para autenticação de usuário")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único da requisição (UUID) gerado pelo servidor", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(value = "requestId", access = JsonProperty.Access.READ_ONLY)
    private UUID requestId;

    @Schema(description = "Identificador de login: e-mail ou CPF (XXX.XXX.XXX-XX ou apenas dígitos)", example = "joao.silva@farmacia.com", required = true)
    @NotBlank(message = "{login.usuario.notBlank}")
    @Size(max = 150, message = "{login.usuario.size}")
    // aceita e-mail válido OU CPF com/sem máscara
    @Pattern(regexp = "^(?:[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "{login.usuario.emailOrCpf.pattern}")
    @JsonProperty("usuario")
    private String usuario;

    @Schema(description = "Senha do usuário", format = "password", required = true, accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "{login.senha.notBlank}")
    @Size(min = 8, max = 128, message = "{login.senha.size}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$", message = "{login.senha.pattern}")
    @JsonProperty(value = "senha", access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Schema(description = "Se deve lembrar sessão (persistente)", example = "false")
    @JsonProperty("lembrarMe")
    private Boolean lembrarMe = Boolean.FALSE;

    @Schema(description = "Código OTP para multifator (6 dígitos)", example = "123456")
    @Pattern(regexp = "\\d{6}", message = "{login.otp.pattern}")
    @JsonProperty("otpCode")
    private String otpCode;

    @Schema(description = "Informações do dispositivo de origem", example = "Chrome/126.0 • Windows 11")
    @Size(max = 200, message = "{login.deviceInfo.size}")
    @JsonProperty("deviceInfo")
    private String deviceInfo;

    @Schema(description = "Endereço IP do cliente (IPv4 ou IPv6)", example = "192.168.0.1")
    @Size(max = 45, message = "{login.ip.size}") // suficiente p/ IPv6
    @Pattern(
            // IPv4 simples OU IPv6 simplificado
            regexp = "^(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-fA-F:]+))$", message = "{login.ip.pattern}")
    @JsonProperty("ipAddress")
    private String ipAddress;

    @Schema(description = "Data/hora da requisição (servidor preenche)", type = "string", format = "date-time", example = "2025-07-04T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @PastOrPresent(message = "{login.requestTime.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "requestTime", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime requestTime;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{login.tenantId.notBlank}")
    @Size(max = 100, message = "{login.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    // ----------------------------------------------------------------------
    // Construtores
    // ----------------------------------------------------------------------
    public LoginRequest() {
    }

    public LoginRequest(UUID requestId, String usuario, String senha, Boolean lembrarMe, String otpCode,
            String deviceInfo, String ipAddress, LocalDateTime requestTime, String tenantId) {
        this.requestId = requestId;
        this.usuario = usuario;
        this.senha = senha;
        this.lembrarMe = lembrarMe;
        this.otpCode = otpCode;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.requestTime = requestTime;
        this.tenantId = tenantId;
    }

    // ----------------------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------------------
    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getLembrarMe() {
        return lembrarMe;
    }

    public void setLembrarMe(Boolean lembrarMe) {
        this.lembrarMe = lembrarMe;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    // ----------------------------------------------------------------------
    // Utilitários
    // ----------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LoginRequest that))
            return false;
        return Objects.equals(requestId, that.requestId) && Objects.equals(usuario, that.usuario)
                && Objects.equals(lembrarMe, that.lembrarMe) && Objects.equals(otpCode, that.otpCode)
                && Objects.equals(deviceInfo, that.deviceInfo) && Objects.equals(ipAddress, that.ipAddress)
                && Objects.equals(requestTime, that.requestTime) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, usuario, lembrarMe, otpCode, deviceInfo, ipAddress, requestTime, tenantId);
    }

    @Override
    public String toString() {
        return "LoginRequest{" + "requestId=" + requestId + ", usuario='" + usuario + '\'' + ", lembrarMe=" + lembrarMe
                + ", otpCode='******'" + // evita logar conteúdo sensível
                ", deviceInfo='" + deviceInfo + '\'' + ", ipAddress='" + ipAddress + '\'' + ", requestTime="
                + requestTime + ", tenantId='" + tenantId + '\'' + '}';
    }
}
