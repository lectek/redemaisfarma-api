// src/main/java/br/com/redemaisfarma/application/service/otp/OtpServiceJpa.java
package br.com.redemaisfarma.application.service.otp;
import org.springframework.context.annotation.Primary;
import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Primary
@Service("otpServiceJpa")
@Profile("prod")
public class OtpServiceJpa implements OtpServicePort {

    public enum Canal { email, sms }

    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);
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
    public StartResult start(String canalStr, String destino, String previousDeliveryId) {
        Canal canal = Canal.valueOf(canalStr.toLowerCase());
        String normalized = normalizeDestino(canal, destino);
        Instant now = Instant.now();

        // cooldown: permite bypass se o previousDeliveryId for o último do destino
        Optional<OtpCodeEntity> last = repo.findFirstByDestinationOrderByCreatedAtDesc(normalized);
        boolean bypass = last.isPresent() && Objects.equals(previousDeliveryId, last.get().getDeliveryId());

        if (!bypass && last.isPresent()) {
            long elapsed = Duration.between(last.get().getCreatedAt(), now).getSeconds();
            if (elapsed < COOLDOWN_SEC) {
                throw new OtpException("cooldown", "Aguarde " + (COOLDOWN_SEC - elapsed) + "s para reenviar.");
            }
        }

        String code = generateCode6();
        String salt = randomSalt();
        String hash = hash(code, salt);

        OtpCodeEntity e = new OtpCodeEntity();
        e.setDeliveryId(UUID.randomUUID().toString());
        e.setDestination(normalized);
        e.setCodeHash(hash);
        e.setSalt(salt);
        e.setTtlSeconds((int) OTP_TTL.getSeconds());
        e.setAttempts(0);
        e.setMaxAttempts(MAX_ATTEMPTS);
        e.setCreatedAt(now);
        e.setExpiresAt(now.plus(OTP_TTL));
        e.setStatus("PENDING");
        repo.save(e);

        sendOtp(canal, normalized, code);

        return new StartResult(
                e.getDeliveryId(),
                maskDestino(normalized),
                COOLDOWN_SEC,
                (int) OTP_TTL.getSeconds(),
                null
        );
    }

    @Override
    @Transactional
    public String verify(String deliveryId, String codeRaw) {
        OtpCodeEntity e = repo.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new OtpException("expired", "Código expirado."));

        Instant now = Instant.now();
        if (now.isAfter(e.getExpiresAt())) {
            e.setStatus("EXPIRED");
            repo.save(e);
            throw new OtpException("expired", "Código expirado.");
        }
        if (e.getAttempts() >= e.getMaxAttempts()) {
            e.setStatus("BLOCKED");
            repo.save(e);
            throw new OtpException("too_many_attempts", "Muitas tentativas. Solicite novo código.");
        }

        String code = (codeRaw == null ? "" : codeRaw.replaceAll("\\D", ""));
        String expected = e.getCodeHash();
        String actual = hash(code, e.getSalt());

        if (!Objects.equals(expected, actual)) {
            e.setAttempts(e.getAttempts() + 1);
            repo.save(e);
            throw new OtpException("invalid", "Código incorreto.");
        }

        e.setStatus("VERIFIED");
        e.setVerifiedAt(now);
        String token = UUID.randomUUID().toString();
        e.setVerificationToken(token);
        repo.save(e);
        return token;
    }

    @Override
    @Transactional
    public boolean consumeToken(String token) {
        if (token == null || token.isBlank()) return false;
        Optional<OtpCodeEntity> opt = repo.findByVerificationToken(token);
        if (opt.isEmpty()) return false;

        OtpCodeEntity e = opt.get();
        Instant now = Instant.now();

        if (!"VERIFIED".equalsIgnoreCase(e.getStatus())
                || e.getVerifiedAt() == null
                || now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL))) {
            return false;
        }
        if (e.getConsumedAt() != null) return false;

        e.setConsumedAt(now);
        repo.save(e);
        return true;
    }

    @Override
    @Transactional
    public boolean consumeTokenForDestino(String token, String destino) {
        if (token == null || token.isBlank()) return false;
        Optional<OtpCodeEntity> opt = repo.findByVerificationToken(token);
        if (opt.isEmpty()) return false;

        OtpCodeEntity e = opt.get();
        Instant now = Instant.now();

        // válido e dentro do TTL do token?
        if (!"VERIFIED".equalsIgnoreCase(e.getStatus())
                || e.getVerifiedAt() == null
                || now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL))) {
            return false;
        }
        if (e.getConsumedAt() != null) return false;

        // checa destino: e-mail case-insensitive; telefone apenas dígitos
        String provided = normalizeByGuess(destino);
        String stored   = e.getDestination();

        boolean match;
        if (provided.contains("@")) {
            match = provided.equalsIgnoreCase(stored);
        } else {
            match = provided.equals(stored);
        }
        if (!match) return false;

        // consome
        e.setConsumedAt(now);
        repo.save(e);
        return true;
    }

    // ===== helpers =====

    private String generateCode6() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String randomSalt() {
        byte[] b = new byte[16];
        rnd.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private String hash(String code, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] out = md.digest(code.getBytes());
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash do OTP", e);
        }
    }

    private String normalizeDestino(Canal canal, String v) {
        if (v == null) return "";
        return (canal == Canal.email) ? v.trim() : v.replaceAll("\\D", "");
    }

    /** Normaliza “no escuro”: se tiver '@' trata como e-mail (trim), senão como telefone (só dígitos). */
    private String normalizeByGuess(String v) {
        if (v == null) return "";
        return v.contains("@") ? v.trim() : v.replaceAll("\\D", "");
    }

    private String maskDestino(String v) {
        if (v.contains("@")) {
            int at = v.indexOf('@');
            if (at <= 1) return "***";
            String name = v.substring(0, at);
            String domain = v.substring(at);
            char first = name.charAt(0);
            char last = name.charAt(Math.max(0, name.length() - 1));
            return first + "****" + last + domain;
        }
        String d = v.replaceAll("\\D", "");
        if (d.length() < 4) return "****";
        return "****" + d.substring(d.length() - 4);
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
                  <hr/>
                  <small>Se não foi você, ignore este e-mail.</small>
                </div>
            """.formatted(code, OTP_TTL.toMinutes());
            mailer.send(destino, subject, html, null);
        } else {
            // SMS opcional (integração futura)
        }
    }
}
