/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.user.Role
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import br.com.redemaisfarma.domain.user.Role;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevProvisioningService {
    private static final SecureRandom RNG = new SecureRandom();
    private final OtpCodeRepository otpRepo;
    private final MailSenderAdapter mailer;
    private final EmailDeliveryRepository emailDeliveryRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final String baseUrl;

    public DevProvisioningService(OtpCodeRepository otpRepo, MailSenderAdapter mailer, EmailDeliveryRepository emailDeliveryRepo, UsuarioJpaRepository usuarioRepo, PasswordEncoder encoder, @Value(value="${app.web.base-url}") String baseUrl) {
        this.otpRepo = otpRepo;
        this.mailer = mailer;
        this.emailDeliveryRepo = emailDeliveryRepo;
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public String start(String email) {
        String code = DevProvisioningService.randomCode6();
        byte[] salt = DevProvisioningService.randomSalt();
        String codeHash = DevProvisioningService.hashCode(code, salt);
        String token = UUID.randomUUID().toString();
        String deliveryId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        int ttlSec = 600;
        OtpCodeEntity otp = new OtpCodeEntity();
        otp.setDeliveryId(deliveryId);
        otp.setDestination(email);
        otp.setCodeHash(codeHash);
        otp.setSalt(Base64.getEncoder().encodeToString(salt));
        otp.setTtlSeconds(ttlSec);
        otp.setAttempts(0);
        otp.setMaxAttempts(3);
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plusSeconds(ttlSec));
        otp.setStatus("PENDING");
        otp.setVerificationToken(token);
        this.otpRepo.save(otp);
        String link = this.baseUrl + "/api/dev/provision/verify?token=" + token;
        String subject = "C\u00f3digo para criar acesso DEV";
        String html = "<div style=\"font-family:Arial,sans-serif\">\n  <h3>Cria\u00e7\u00e3o de acesso DEV</h3>\n  <p>Seu c\u00f3digo \u00e9 <b style=\"font-size:18px\">%s</b> (expira em %d min).</p>\n  <p>Ou clique: <a href=\"%s\">%s</a></p>\n</div>\n".formatted(code, ttlSec / 60, link, link);
        this.mailer.send(email, subject, html);
        EmailDelivery ed = new EmailDelivery();
        ed.setPurpose("DEV_CREATE_CODE");
        ed.setDestination(email);
        ed.setProvider("SMTP");
        ed.setStatus("SENT");
        ed.setAttempts(1);
        ed.setMessageId(null);
        ed.setPayloadJson("{\"subject\":\"%s\",\"link\":\"%s\",\"deliveryId\":\"%s\"}\n".formatted(subject, link, deliveryId));
        this.emailDeliveryRepo.save(ed);
        return deliveryId;
    }

    @Transactional
    public void verifyByToken(String token, String optionalPassword, String cpfIfNew, String nomeIfNew) {
        UsuarioEntity user;
        OtpCodeEntity otp = this.otpRepo.findByVerificationToken(token).orElseThrow(() -> new IllegalArgumentException("token inv\u00e1lido"));
        Instant now = Instant.now();
        if (!"PENDING".equalsIgnoreCase(otp.getStatus()) || now.isAfter(otp.getExpiresAt())) {
            throw new IllegalStateException("c\u00f3digo expirado ou j\u00e1 utilizado");
        }
        otp.setStatus("VERIFIED");
        otp.setVerifiedAt(now);
        otp.setConsumedAt(now);
        this.otpRepo.save(otp);
        String email = otp.getDestination();
        Optional<UsuarioEntity> existing = this.usuarioRepo.findByEmailOrCpf(email);
        if (existing.isPresent()) {
            user = existing.get();
        } else {
            if (cpfIfNew == null || cpfIfNew.isBlank()) {
                throw new IllegalArgumentException("CPF \u00e9 obrigat\u00f3rio para cria\u00e7\u00e3o do usu\u00e1rio.");
            }
            if (nomeIfNew == null || nomeIfNew.isBlank()) {
                throw new IllegalArgumentException("Nome \u00e9 obrigat\u00f3rio para cria\u00e7\u00e3o do usu\u00e1rio.");
            }
            user = new UsuarioEntity();
            user.setEmail(email);
            user.setCpf(cpfIfNew);
            user.setNome(nomeIfNew);
        }
        if (optionalPassword != null && !optionalPassword.isBlank()) {
            user.setSenha(this.encoder.encode((CharSequence)optionalPassword));
        } else if (user.getSenha() == null || user.getSenha().isBlank()) {
            user.setSenha(this.encoder.encode((CharSequence)UUID.randomUUID().toString()));
        }
        user.addRole(Role.of((String)"ROLE_DEVELOPER"));
        this.usuarioRepo.save(user);
    }

    private static String randomCode6() {
        return String.format("%06d", RNG.nextInt(1000000));
    }

    private static byte[] randomSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return salt;
    }

    private static String hashCode(String code, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return Base64.getEncoder().encodeToString(md.digest(code.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            throw new IllegalStateException("hash failure", e);
        }
    }
}

