// src/main/java/br/com/redemaisfarma/application/service/otp/OtpService.java
package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("otpService")
@Profile({"dev", "test"})
public class OtpService implements OtpServicePort { // << implementa a porta

    public enum Canal { email, sms }

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SEC = 60;

    private final SecureRandom rnd = new SecureRandom();

    // tipos internos só para estado
    private record OtpEntry(
        String canal, String destino, String code,
        Instant createdAt, Instant expiresAt, int attempts
    ) {}
    private record TokenEntry(String deliveryId, String destinoNormalizado, Instant expiresAt) {}

    private final Map<String, OtpEntry> deliveries = new ConcurrentHashMap<>();
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSendByKey = new ConcurrentHashMap<>();
    private final Map<String, String> lastDeliveryIdByKey = new ConcurrentHashMap<>();

    private final MailSenderAdapter mailer;
    private final Environment env;

    public OtpService(MailSenderAdapter mailer, Environment env) {
        this.mailer = mailer;
        this.env = env;
    }

    @Override
    public StartResult start(String canalStr, String destino, String previousDeliveryId) {
        Canal canal = Canal.valueOf(canalStr.toLowerCase());
        String normalizedDestino = normalizeDestino(canal, destino);
        String key = key(canal, normalizedDestino);

        Instant now = Instant.now();
        Instant last = lastSendByKey.get(key);

        boolean bypassCooldown = false;
        if (previousDeliveryId != null && !previousDeliveryId.isBlank()) {
            String lastDeliveryForKey = lastDeliveryIdByKey.get(key);
            bypassCooldown = previousDeliveryId.equals(lastDeliveryForKey);
        }

        if (!bypassCooldown && last != null) {
            long secSinceLast = Duration.between(last, now).getSeconds();
            if (secSinceLast < COOLDOWN_SEC) {
                throw new OtpException(
                    "cooldown",
                    "Aguarde " + (COOLDOWN_SEC - secSinceLast) + "s para reenviar."
                );
            }
        }

        String code = generateCode6();
        String deliveryId = UUID.randomUUID().toString();

        OtpEntry entry = new OtpEntry(
            canal.name(), normalizedDestino, code, now, now.plus(OTP_TTL), 0
        );
        deliveries.put(deliveryId, entry);
        lastSendByKey.put(key, now);
        lastDeliveryIdByKey.put(key, deliveryId);

        sendOtp(canal, normalizedDestino, code);

        String demoCode = isProd() ? null : code;
        return new StartResult(
            deliveryId,
            maskDestino(canal, normalizedDestino),
            COOLDOWN_SEC,
            (int) OTP_TTL.getSeconds(),
            demoCode
        );
    }

    @Override
    public String verify(String deliveryId, String codeRaw) {
        OtpEntry e = deliveries.get(deliveryId);
        if (e == null) throw new OtpException("expired", "Código expirado.");

        Instant now = Instant.now();
        if (now.isAfter(e.expiresAt)) {
            deliveries.remove(deliveryId);
            throw new OtpException("expired", "Código expirado.");
        }
        if (e.attempts >= MAX_ATTEMPTS) {
            deliveries.remove(deliveryId);
            throw new OtpException("too_many_attempts", "Muitas tentativas. Solicite novo código.");
        }

        String code = (codeRaw == null ? "" : codeRaw.replaceAll("\\D", ""));
        if (!Objects.equals(e.code, code)) {
            deliveries.put(deliveryId, new OtpEntry(
                e.canal, e.destino, e.code, e.createdAt, e.expiresAt, e.attempts + 1
            ));
            throw new OtpException("invalid", "Código incorreto.");
        }

        deliveries.remove(deliveryId);
        String token = UUID.randomUUID().toString();
        // guardamos o destino normalizado dentro do token para amarração posterior
        tokens.put(token, new TokenEntry(deliveryId, e.destino, now.plus(TOKEN_TTL)));
        return token;
    }

    @Override
    public boolean consumeToken(String token) {
        TokenEntry te = tokens.remove(token);
        return te != null && Instant.now().isBefore(te.expiresAt);
    }

    @Override
    public boolean consumeTokenForDestino(String token, String destino) {
        if (token == null || token.isBlank()) return false;
        TokenEntry te = tokens.remove(token);
        if (te == null) return false;
        if (Instant.now().isAfter(te.expiresAt)) return false;

        // normaliza “chute” do canal: e-mail se contiver '@', senão SMS
        String normalizedInput = normalizeByGuess(destino);

        // para e-mail, usar comparação case-insensitive; para telefone, exata (apenas dígitos)
        boolean isEmail = normalizedInput.contains("@");
        if (isEmail) {
            return normalizedInput.equalsIgnoreCase(te.destinoNormalizado);
        }
        return normalizedInput.equals(te.destinoNormalizado);
    }

    // ---------- helpers ----------
    private boolean isProd() {
        for (String p : env.getActiveProfiles()) if ("prod".equalsIgnoreCase(p)) return true;
        return false;
    }
    private String key(Canal canal, String destino) { return canal.name() + '|' + destino; }
    private String generateCode6() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
    private String normalizeDestino(Canal canal, String v) {
        if (v == null) return "";
        if (canal == Canal.email) return v.trim();
        return v.replaceAll("\\D", "");
    }
    private String normalizeByGuess(String v) {
        if (v == null) return "";
        if (v.contains("@")) return v.trim();        // e-mail
        return v.replaceAll("\\D", "");              // telefone
    }
    private String maskDestino(Canal canal, String v) {
        if (canal == Canal.email) {
            int at = v.indexOf('@');
            if (at <= 1) return "***";
            String name = v.substring(0, at), domain = v.substring(at);
            char first = name.charAt(0);
            char last = name.charAt(Math.max(0, name.length() - 1));
            return first + "****" + last + domain;
        } else {
            String d = v.replaceAll("\\D", "");
            if (d.length() < 4) return "****";
            return "****" + d.substring(d.length() - 4);
        }
    }
    private void sendOtp(Canal canal, String destino, String code) {
        if (canal == Canal.email) {
            String subject = "Seu código RedeMaisFarma";
            String html = """
                <div style="font-family:system-ui,Segoe UI,Arial,sans-serif">
                  <h2>Confirme seu cadastro</h2>
                  <p>Use este código para verificar seu e-mail:</p>
                  <p style="font-size:24px;letter-spacing:6px"><b>%s</b></p>
                  <p>Ele expira em %d minutos.</p>
                  <hr/><small>Se não foi você, ignore este e-mail.</small>
                </div>
            """.formatted(code, OTP_TTL.toMinutes());
            mailer.send(destino, subject, html, null);
            log.info("OTP email sent: to={} code=**** (masked)", destino);
        } else {
            log.info("OTP sms requested: to={} code=**** (masked) [NO-OP]", destino);
        }
    }
}
