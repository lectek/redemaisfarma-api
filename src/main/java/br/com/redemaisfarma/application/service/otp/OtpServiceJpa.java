/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Primary
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Service(value="otpServiceJpa")
@Profile(value={"prod"})
public class OtpServiceJpa
implements OtpServicePort {
    private static final Duration OTP_TTL = Duration.ofMinutes(10L);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10L);
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SEC = 60;
    private final SecureRandom rnd = new SecureRandom();
    private final MailSenderAdapter mailer;
    private final OtpCodeRepository repo;

    public OtpServiceJpa(MailSenderAdapter mailer, OtpCodeRepository repo) {
        this.mailer = mailer;
        this.repo = repo;
    }

    @Override
    @Transactional
    public OtpServicePort.StartResult start(String canalStr, String destino, String previousDeliveryId) {
        long elapsed;
        Canal canal = Canal.valueOf(canalStr.toLowerCase());
        String normalized = this.normalizeDestino(canal, destino);
        Instant now = Instant.now();
        Optional<OtpCodeEntity> last = this.repo.findFirstByDestinationOrderByCreatedAtDesc(normalized);
        boolean bypass = last.isPresent() && Objects.equals(previousDeliveryId, last.get().getDeliveryId());
        boolean bl = bypass;
        if (!bypass && last.isPresent() && (elapsed = Duration.between(last.get().getCreatedAt(), now).getSeconds()) < 60L) {
            throw new OtpServicePort.OtpException("cooldown", "Aguarde " + (60L - elapsed) + "s para reenviar.");
        }
        String code = this.generateCode6();
        String salt = this.randomSalt();
        String hash = this.hash(code, salt);
        OtpCodeEntity e = new OtpCodeEntity();
        e.setDeliveryId(UUID.randomUUID().toString());
        e.setDestination(normalized);
        e.setCodeHash(hash);
        e.setSalt(salt);
        e.setTtlSeconds((int)OTP_TTL.getSeconds());
        e.setAttempts(0);
        e.setMaxAttempts(5);
        e.setCreatedAt(now);
        e.setExpiresAt(now.plus(OTP_TTL));
        e.setStatus("PENDING");
        this.repo.save(e);
        this.sendOtp(canal, normalized, code);
        return new OtpServicePort.StartResult(e.getDeliveryId(), this.maskDestino(normalized), 60, (int)OTP_TTL.getSeconds(), null);
    }

    @Override
    @Transactional
    public String verify(String deliveryId, String codeRaw) {
        String actual;
        OtpCodeEntity e = this.repo.findByDeliveryId(deliveryId).orElseThrow(() -> new OtpServicePort.OtpException("expired", "C\u00f3digo expirado."));
        Instant now = Instant.now();
        if (now.isAfter(e.getExpiresAt())) {
            e.setStatus("EXPIRED");
            this.repo.save(e);
            throw new OtpServicePort.OtpException("expired", "C\u00f3digo expirado.");
        }
        if (e.getAttempts() >= e.getMaxAttempts()) {
            e.setStatus("BLOCKED");
            this.repo.save(e);
            throw new OtpServicePort.OtpException("too_many_attempts", "Muitas tentativas. Solicite novo c\u00f3digo.");
        }
        String code = codeRaw == null ? "" : codeRaw.replaceAll("\\D", "");
        String expected = e.getCodeHash();
        if (!Objects.equals(expected, actual = this.hash(code, e.getSalt()))) {
            e.setAttempts(e.getAttempts() + 1);
            this.repo.save(e);
            throw new OtpServicePort.OtpException("invalid", "C\u00f3digo incorreto.");
        }
        e.setStatus("VERIFIED");
        e.setVerifiedAt(now);
        String token = UUID.randomUUID().toString();
        e.setVerificationToken(token);
        this.repo.save(e);
        return token;
    }

    @Override
    @Transactional
    public boolean consumeToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Optional<OtpCodeEntity> opt = this.repo.findByVerificationToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        OtpCodeEntity e = opt.get();
        Instant now = Instant.now();
        if (!"VERIFIED".equalsIgnoreCase(e.getStatus()) || e.getVerifiedAt() == null || now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL))) {
            return false;
        }
        if (e.getConsumedAt() != null) {
            return false;
        }
        e.setConsumedAt(now);
        this.repo.save(e);
        return true;
    }

    @Override
    @Transactional
    public boolean consumeTokenForDestino(String token, String destino) {
        boolean match;
        if (token == null || token.isBlank()) {
            return false;
        }
        Optional<OtpCodeEntity> opt = this.repo.findByVerificationToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        OtpCodeEntity e = opt.get();
        Instant now = Instant.now();
        if (!"VERIFIED".equalsIgnoreCase(e.getStatus()) || e.getVerifiedAt() == null || now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL))) {
            return false;
        }
        if (e.getConsumedAt() != null) {
            return false;
        }
        String provided = this.normalizeByGuess(destino);
        String stored = e.getDestination();
        boolean bl = match = provided.contains("@") ? provided.equalsIgnoreCase(stored) : provided.equals(stored);
        if (!match) {
            return false;
        }
        e.setConsumedAt(now);
        this.repo.save(e);
        return true;
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

    private String randomSalt() {
        byte[] b = new byte[16];
        this.rnd.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private String hash(String code, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] out = md.digest(code.getBytes());
            return HexFormat.of().formatHex(out);
        }
        catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash do OTP", e);
        }
    }

    private String normalizeDestino(Canal canal, String v) {
        if (v == null) {
            return "";
        }
        return canal == Canal.email ? v.trim() : v.replaceAll("\\D", "");
    }

    private String normalizeByGuess(String v) {
        if (v == null) {
            return "";
        }
        return v.contains("@") ? v.trim() : v.replaceAll("\\D", "");
    }

    private String maskDestino(String v) {
        if (v.contains("@")) {
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
            String html = "    <div style=\"font-family:system-ui,Segoe UI,Arial,sans-serif\">\n      <h2>Confirme seu cadastro</h2>\n      <p>Use este c\u00f3digo para verificar seu e-mail:</p>\n      <p style=\"font-size:24px;letter-spacing:6px\"><b>%s</b></p>\n      <p>Ele expira em %d minutos.</p>\n      <hr/>\n      <small>Se n\u00e3o foi voc\u00ea, ignore este e-mail.</small>\n    </div>\n".formatted(code, OTP_TTL.toMinutes());
            this.mailer.send(destino, subject, html, null);
        }
    }

    public static enum Canal {
        email,
        sms;

    }
}

