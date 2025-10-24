/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.AssertTrue$List
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.validation.annotation.Validated
 */
package br.com.redemaisfarma.adapters.outbound.email.config;

import jakarta.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix="app.mail")
public class AppMailProperties {
    private boolean enabled = false;
    private String from;
    private List<String> bcc = new ArrayList<String>();
    private String cssUrl;
    private int sendTimeoutMs = 10000;

    @AssertTrue(message="app.mail.from deve ser informado quando app.mail.enabled=true")
@AssertTrue(message="app.mail.from deve ser informado quando app.mail.enabled=true")
    public @AssertTrue(message="app.mail.from deve ser informado quando app.mail.enabled=true")
@AssertTrue(message="app.mail.from deve ser informado quando app.mail.enabled=true") boolean isFromValidWhenEnabled() {
        return !this.enabled || this.from != null && !this.from.isBlank();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getBcc() {
        return this.bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc == null ? new ArrayList() : bcc;
    }

    public String getCssUrl() {
        return this.cssUrl;
    }

    public void setCssUrl(String cssUrl) {
        this.cssUrl = cssUrl;
    }

    public int getSendTimeoutMs() {
        return this.sendTimeoutMs;
    }

    public void setSendTimeoutMs(int sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public boolean hasGlobalBcc() {
        return this.bcc != null && !this.bcc.isEmpty();
    }
}

