package br.com.redemaisfarma.adapters.outbound.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Propriedades de JWT/Autenticação.
 *
 * Bind automático a partir de application.yml (prefixo "jwt"). Esta classe é usada pelos adapters de geração/validação
 * de tokens, armazenamento de refresh tokens e por filtros de autenticação.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Segredo usado para assinar/verificar os tokens (HMAC). Em produção, injete via variável de ambiente/secret
     * manager.
     */
    @NotBlank
    private String secret;

    /**
     * Emissor (iss) incluído no token.
     */
    @NotBlank
    private String issuer;

    /**
     * Audiência (aud) opcional para reforçar validação entre serviços.
     */
    private String audience;

    /**
     * Tempo de expiração do access token, em minutos.
     */
    @NotNull
    @Min(1)
    private Integer accessTokenExpirationMinutes;

    /**
     * Tempo de expiração do refresh token, em minutos.
     */
    @NotNull
    @Min(10)
    private Integer refreshTokenExpirationMinutes;

    /**
     * Tolerância de clock (em segundos) para validação de "nbf" e "exp".
     */
    @NotNull
    @Min(0)
    private Integer clockSkewSeconds = 60;

    /**
     * Cabeçalho HTTP onde o token é esperado (ex.: Authorization).
     */
    @NotBlank
    private String header = "Authorization";

    /**
     * Prefixo do header (ex.: Bearer).
     */
    @NotBlank
    private String prefix = "Bearer";

    /**
     * Nome do cookie usado para refresh token (quando aplicável).
     */
    private String refreshCookieName = "REFRESH_TOKEN";

    /**
     * Sinalizador de segurança do cookie de refresh.
     */
    private boolean refreshCookieSecure = true;

    /**
     * Sinalizador HttpOnly do cookie de refresh.
     */
    private boolean refreshCookieHttpOnly = true;

    /**
     * Domain opcional para o cookie de refresh.
     */
    private String refreshCookieDomain;

    /**
     * Path do cookie de refresh.
     */
    private String refreshCookiePath = "/";

    // =========================
    // Getters e Setters
    // =========================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Integer getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(Integer accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public Integer getRefreshTokenExpirationMinutes() {
        return refreshTokenExpirationMinutes;
    }

    public void setRefreshTokenExpirationMinutes(Integer refreshTokenExpirationMinutes) {
        this.refreshTokenExpirationMinutes = refreshTokenExpirationMinutes;
    }

    public Integer getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(Integer clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public void setRefreshCookieName(String refreshCookieName) {
        this.refreshCookieName = refreshCookieName;
    }

    public boolean isRefreshCookieSecure() {
        return refreshCookieSecure;
    }

    public void setRefreshCookieSecure(boolean refreshCookieSecure) {
        this.refreshCookieSecure = refreshCookieSecure;
    }

    public boolean isRefreshCookieHttpOnly() {
        return refreshCookieHttpOnly;
    }

    public void setRefreshCookieHttpOnly(boolean refreshCookieHttpOnly) {
        this.refreshCookieHttpOnly = refreshCookieHttpOnly;
    }

    public String getRefreshCookieDomain() {
        return refreshCookieDomain;
    }

    public void setRefreshCookieDomain(String refreshCookieDomain) {
        this.refreshCookieDomain = refreshCookieDomain;
    }

    public String getRefreshCookiePath() {
        return refreshCookiePath;
    }

    public void setRefreshCookiePath(String refreshCookiePath) {
        this.refreshCookiePath = refreshCookiePath;
    }
}