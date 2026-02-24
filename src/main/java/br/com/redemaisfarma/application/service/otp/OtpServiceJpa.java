package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import org.springframework.context.annotation.Primary;
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

/**
 * ImplementaÃ§Ã£o padrÃ£o do serviÃ§o de OTP usando JPA.
 * ProduÃ§Ã£o: ativa apenas em profile "prod".
 */
@Primary
@Service("otpServiceJpa")
@Profile("prod")
public class OtpServiceJpa implements OtpServicePort {

    private static final Duration OTP_TTL     = Duration.ofMinutes(10);
    private static final Duration TOKEN_TTL   = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS     = 5;
    private static final int COOLDOWN_SECONDS = 60;

    private final SecureRandom rnd = new SecureRandom();
    private final MailSenderAdapter mailer;
    private final OtpCodeRepository repo;

    public OtpServiceJpa(MailSenderAdapter mailer, OtpCodeRepository repo) {
        this.mailer = Objects.requireNonNull(mailer);
        this.repo   = Objects.requireNonNull(repo);
    }

    @Override
    @Transactional
    public StartResult start(String canalStr, String destino, String previousDeliveryId) {
        Canal canal = Canal.valueOf(canalStr.toLowerCase());
        String normalized = normalizeDestino(canal, destino);
        Instant now = Instant.now();

        Optional<OtpCodeEntity> lastOpt = repo.findFirstByDestinationOrderByCreatedAtDesc(normalized);
        boolean bypass = lastOpt.isPresent() && Objects.equals(previousDeliveryId, lastOpt.get().getDeliveryId());

        if (!bypass && lastOpt.isPresent()) {
            long elapsed = Duration.between(lastOpt.get().getCreatedAt(), now).getSeconds();
            if (elapsed < COOLDOWN_SECONDS) {
                throw new OtpException("cooldown", "Aguarde " + (COOLDOWN_SECONDS - elapsed) + "s para reenviar.");
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
                COOLDOWN_SECONDS,
                (int) OTP_TTL.getSeconds(),
                null
        );
    }

    @Override
    @Transactional
    public String verify(String deliveryId, String codeRaw) {
        OtpCodeEntity e = repo.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new OtpException("expired", "CÃ³digo expirado."));

        Instant now = Instant.now();
        if (now.isAfter(e.getExpiresAt())) {
            e.setStatus("EXPIRED");
            repo.save(e);
            throw new OtpException("expired", "CÃ³digo expirado.");
        }

        if (e.getAttempts() >= e.getMaxAttempts()) {
            e.setStatus("BLOCKED");
            repo.save(e);
            throw new OtpException("too_many_attempts", "Muitas tentativas. Solicite novo cÃ³digo.");
        }

        String codeSanitized = codeRaw == null ? "" : codeRaw.replaceAll("\\D", "");
        String actualHash = hash(codeSanitized, e.getSalt());

        if (!Objects.equals(e.getCodeHash(), actualHash)) {
            e.setAttempts(e.getAttempts() + 1);
            repo.save(e);
            throw new OtpException("invalid", "CÃ³digo incorreto.");
        }

        e.setStatus("VERIFIED");
        e.setVerifiedAt(now);
        e.setVerificationToken(UUID.randomUUID().toString());
        repo.save(e);

        return e.getVerificationToken();
    }

    @Override
    @Transactional
    public boolean consumeToken(String token) {
        if (token == null || token.isBlank()) return false;

        Optional<OtpCodeEntity> opt = repo.findByVerificationToken(token);
        if (opt.isEmpty()) return false;

        OtpCodeEntity e = opt.get();
        Instant now = Instant.now();

        if (!"VERIFIED".equalsIgnoreCase(e.getStatus()) ||
            e.getVerifiedAt() == null ||
            now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL)) ||
            e.getConsumedAt() != null) {
            return false;
        }

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

        if (!"VERIFIED".equalsIgnoreCase(e.getStatus()) ||
            e.getVerifiedAt() == null ||
            now.isAfter(e.getVerifiedAt().plus(TOKEN_TTL)) ||
            e.getConsumedAt() != null) {
            return false;
        }

        String provided = normalizeByGuess(destino);
        String stored = e.getDestination();
        boolean match = provided.contains("@")
                ? provided.equalsIgnoreCase(stored)
                : provided.equals(stored);

        if (!match) return false;

        e.setConsumedAt(now);
        repo.save(e);
        return true;
    }

    /* === MÃ©todos utilitÃ¡rios internos === */

    private String generateCode6() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(rnd.nextInt(10));
        }
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
        return canal == Canal.email ? v.trim() : v.replaceAll("\\D", "");
    }

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
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 4) return "****";
        return "****" + digits.substring(digits.length() - 4);
    }

    private void sendOtp(Canal canal, String destino, String code) {
        if (canal == Canal.sms) {
            throw new OtpException(
                    "sms_unavailable",
                    "Envio por SMS indisponivel no momento. Selecione E-mail para receber o codigo."
            );
        }

        String subject = "Seu codigo RedeMaisFarma";
        String html = """
                <div style="font-family:system-ui,Segoe UI,Arial,sans-serif">
                  <h2>Confirme seu cadastro</h2>
                  <p>Use este codigo para verificar seu e-mail:</p>
                  <p style="font-size:24px;letter-spacing:6px"><b>%s</b></p>
                  <p>Ele expira em %d minutos.</p>
                  <hr/>
                  <small>Se nao foi voce, ignore este e-mail.</small>
                </div>
                """.formatted(code, OTP_TTL.toMinutes());
        mailer.send(destino, subject, html, null);
    }

    public enum Canal {
        email, sms
    }
}
