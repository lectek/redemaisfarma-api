/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.annotation.Profile
 *  org.springframework.core.env.Environment
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service(value="otpService")
@Profile(value={"dev", "test"})
public class OtpService
implements OtpServicePort {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final Duration OTP_TTL = Duration.ofMinutes(10L);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10L);
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SEC = 60;
    private final SecureRandom rnd = new SecureRandom();
    private final Map<String, OtpEntry> deliveries = new ConcurrentHashMap<String, OtpEntry>();
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<String, TokenEntry>();
    private final Map<String, Instant> lastSendByKey = new ConcurrentHashMap<String, Instant>();
    private final Map<String, String> lastDeliveryIdByKey = new ConcurrentHashMap<String, String>();
    private final MailSenderAdapter mailer;
    private final Environment env;

    public OtpService(MailSenderAdapter mailer, Environment env) {
        this.mailer = mailer;
        this.env = env;
    }

    @Override
    public OtpServicePort.StartResult start(String canalStr, String destino, String previousDeliveryId) {
        long secSinceLast;
        Canal canal = Canal.valueOf(canalStr.toLowerCase());
        String normalizedDestino = this.normalizeDestino(canal, destino);
        String key = this.key(canal, normalizedDestino);
        Instant now = Instant.now();
        Instant last = this.lastSendByKey.get(key);
        boolean bypassCooldown = false;
        if (previousDeliveryId != null && !previousDeliveryId.isBlank()) {
            String lastDeliveryForKey = this.lastDeliveryIdByKey.get(key);
            bypassCooldown = previousDeliveryId.equals(lastDeliveryForKey);
        }
        if (!bypassCooldown && last != null && (secSinceLast = Duration.between(last, now).getSeconds()) < 60L) {
            throw new OtpServicePort.OtpException("cooldown", "Aguarde " + (60L - secSinceLast) + "s para reenviar.");
        }
        String code = this.generateCode6();
        String deliveryId = UUID.randomUUID().toString();
        OtpEntry entry = new OtpEntry(canal.name(), normalizedDestino, code, now, now.plus(OTP_TTL), 0);
        this.deliveries.put(deliveryId, entry);
        this.lastSendByKey.put(key, now);
        this.lastDeliveryIdByKey.put(key, deliveryId);
        this.sendOtp(canal, normalizedDestino, code);
        String demoCode = this.isProd() ? null : code;
        return new OtpServicePort.StartResult(deliveryId, this.maskDestino(canal, normalizedDestino), 60, (int)OTP_TTL.getSeconds(), demoCode);
    }

    @Override
    public String verify(String deliveryId, String codeRaw) {
        OtpEntry e = this.deliveries.get(deliveryId);
        if (e == null) {
            throw new OtpServicePort.OtpException("expired", "C\u00f3digo expirado.");
        }
        Instant now = Instant.now();
        if (now.isAfter(e.expiresAt)) {
            this.deliveries.remove(deliveryId);
            throw new OtpServicePort.OtpException("expired", "C\u00f3digo expirado.");
        }
        if (e.attempts >= 5) {
            this.deliveries.remove(deliveryId);
            throw new OtpServicePort.OtpException("too_many_attempts", "Muitas tentativas. Solicite novo c\u00f3digo.");
        }
        String code = codeRaw == null ? "" : codeRaw.replaceAll("\\D", "");
        String string = code;
        if (!Objects.equals(e.code, code)) {
            this.deliveries.put(deliveryId, new OtpEntry(e.canal, e.destino, e.code, e.createdAt, e.expiresAt, e.attempts + 1));
            throw new OtpServicePort.OtpException("invalid", "C\u00f3digo incorreto.");
        }
        this.deliveries.remove(deliveryId);
        String token = UUID.randomUUID().toString();
        this.tokens.put(token, new TokenEntry(deliveryId, e.destino, now.plus(TOKEN_TTL)));
        return token;
    }

    @Override
    public boolean consumeToken(String token) {
        TokenEntry te = this.tokens.remove(token);
        return te != null && Instant.now().isBefore(te.expiresAt);
    }

    @Override
    public boolean consumeTokenForDestino(String token, String destino) {
        if (token == null || token.isBlank()) {
            return false;
        }
        TokenEntry te = this.tokens.remove(token);
        if (te == null) {
            return false;
        }
        if (Instant.now().isAfter(te.expiresAt)) {
            return false;
        }
        String normalizedInput = this.normalizeByGuess(destino);
        boolean isEmail = normalizedInput.contains("@");
        if (isEmail) {
            return normalizedInput.equalsIgnoreCase(te.destinoNormalizado);
        }
        return normalizedInput.equals(te.destinoNormalizado);
    }

    private boolean isProd() {
        String[] stringArray = this.env.getActiveProfiles();
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String p = stringArray[n2];
            if ("prod".equalsIgnoreCase(p)) {
                return true;
            }
            ++n2;
        }
        return false;
    }

    private String key(Canal canal, String destino) {
        return canal.name() + "|" + destino;
    }

    private String generateCode6() {
        StringBuilder sb = new StringBuilder(6);
        int i = 0;
        while (i < 6) {
            sb.append(this.rnd.nextInt(10));
            ++i;
        }
        return sb.toString();
    }

    private String normalizeDestino(Canal canal, String v) {
        if (v == null) {
            return "";
        }
        if (canal == Canal.email) {
            return v.trim();
        }
        return v.replaceAll("\\D", "");
    }

    private String normalizeByGuess(String v) {
        if (v == null) {
            return "";
        }
        if (v.contains("@")) {
            return v.trim();
        }
        return v.replaceAll("\\D", "");
    }

    private String maskDestino(Canal canal, String v) {
        if (canal == Canal.email) {
            int at = v.indexOf(64);
            if (at <= 1) {
                return "***";
            }
            String name = v.substring(0, at);
            String domain = v.substring(at);
            char first = name.charAt(0);
            char last = name.charAt(Math.max(0, name.length() - 1));
            return first + "****" + last + domain;
        }
        String d = v.replaceAll("\\D", "");
        if (d.length() < 4) {
            return "****";
        }
        return "****" + d.substring(d.length() - 4);
    }

    private void sendOtp(Canal canal, String destino, String code) {
        if (canal == Canal.email) {
            String subject = "Seu c\u00f3digo RedeMaisFarma";
            String html = "    <div style=\"font-family:system-ui,Segoe UI,Arial,sans-serif\">\n      <h2>Confirme seu cadastro</h2>\n      <p>Use este c\u00f3digo para verificar seu e-mail:</p>\n      <p style=\"font-size:24px;letter-spacing:6px\"><b>%s</b></p>\n      <p>Ele expira em %d minutos.</p>\n      <hr/><small>Se n\u00e3o foi voc\u00ea, ignore este e-mail.</small>\n    </div>\n".formatted(code, OTP_TTL.toMinutes());
            this.mailer.send(destino, subject, html, null);
            log.info("OTP email sent: to={} code=**** (masked)", (Object)destino);
        } else {
            log.info("OTP sms requested: to={} code=**** (masked) [NO-OP]", (Object)destino);
        }
    }

    public static enum Canal {
        email,
        sms;

    }

    private record OtpEntry(String canal, String destino, String code, Instant createdAt, Instant expiresAt, int attempts) {
    }

    private record TokenEntry(String deliveryId, String destinoNormalizado, Instant expiresAt) {
    }
}

