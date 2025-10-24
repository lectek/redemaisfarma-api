/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.Min$List
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.NotNull$List
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.validation.annotation.Validated
 */
package br.com.redemaisfarma.adapters.outbound.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix="jwt")
public class JwtProperties {
    @NotBlank
    private String secret;
    @NotBlank
    private String issuer;
    private String audience;
    @NotNull, @NotNull
    @Min(value=1L)
@Min(value=1L)
    private @NotNull, @NotNull @Min(value=1L)
@Min(value=1L) Integer accessTokenExpirationMinutes;
    @NotNull, @NotNull
    @Min(value=10L)
@Min(value=10L)
    private @NotNull, @NotNull @Min(value=10L)
@Min(value=10L) Integer refreshTokenExpirationMinutes;
    @NotNull, @NotNull
    @Min(value=0L)
@Min(value=0L)
    private @NotNull, @NotNull @Min(value=0L)
@Min(value=0L) Integer clockSkewSeconds = 60;
    @NotBlank
    private String header = "Authorization";
    @NotBlank
    private String prefix = "Bearer";
    private String refreshCookieName = "REFRESH_TOKEN";
    private boolean refreshCookieSecure = true;
    private boolean refreshCookieHttpOnly = true;
    private String refreshCookieDomain;
    private String refreshCookiePath = "/";

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return this.audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Integer getAccessTokenExpirationMinutes() {
        return this.accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(Integer accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public Integer getRefreshTokenExpirationMinutes() {
        return this.refreshTokenExpirationMinutes;
    }

    public void setRefreshTokenExpirationMinutes(Integer refreshTokenExpirationMinutes) {
        this.refreshTokenExpirationMinutes = refreshTokenExpirationMinutes;
    }

    public Integer getClockSkewSeconds() {
        return this.clockSkewSeconds;
    }

    public void setClockSkewSeconds(Integer clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public String getHeader() {
        return this.header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRefreshCookieName() {
        return this.refreshCookieName;
    }

    public void setRefreshCookieName(String refreshCookieName) {
        this.refreshCookieName = refreshCookieName;
    }

    public boolean isRefreshCookieSecure() {
        return this.refreshCookieSecure;
    }

    public void setRefreshCookieSecure(boolean refreshCookieSecure) {
        this.refreshCookieSecure = refreshCookieSecure;
    }

    public boolean isRefreshCookieHttpOnly() {
        return this.refreshCookieHttpOnly;
    }

    public void setRefreshCookieHttpOnly(boolean refreshCookieHttpOnly) {
        this.refreshCookieHttpOnly = refreshCookieHttpOnly;
    }

    public String getRefreshCookieDomain() {
        return this.refreshCookieDomain;
    }

    public void setRefreshCookieDomain(String refreshCookieDomain) {
        this.refreshCookieDomain = refreshCookieDomain;
    }

    public String getRefreshCookiePath() {
        return this.refreshCookiePath;
    }

    public void setRefreshCookiePath(String refreshCookiePath) {
        this.refreshCookiePath = refreshCookiePath;
    }
}

