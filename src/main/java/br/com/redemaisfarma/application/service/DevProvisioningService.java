// src/main/java/br/com/redemaisfarma/application/service/DevProvisioningService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class DevProvisioningService {

  private static final SecureRandom RNG = new SecureRandom();

  private final OtpCodeRepository otpRepo;
  private final MailSenderAdapter mailer;
  private final EmailDeliveryRepository emailDeliveryRepo;
  private final UsuarioJpaRepository usuarioRepo;
  private final PasswordEncoder encoder;
  private final String baseUrl;

  public DevProvisioningService(OtpCodeRepository otpRepo,
                                MailSenderAdapter mailer,
                                EmailDeliveryRepository emailDeliveryRepo,
                                UsuarioJpaRepository usuarioRepo,
                                PasswordEncoder encoder,
                                @Value("${app.web.base-url}") String baseUrl) {
    this.otpRepo = otpRepo;
    this.mailer = mailer;
    this.emailDeliveryRepo = emailDeliveryRepo;
    this.usuarioRepo = usuarioRepo;
    this.encoder = encoder;
    this.baseUrl = baseUrl;
  }

  /** Inicia: gera código + token e envia e-mail. */
  @Transactional
  public String start(String email) {
    String code = randomCode6();
    byte[] salt = randomSalt();
    String codeHash = hashCode(code, salt);
    String token = UUID.randomUUID().toString();
    String deliveryId = UUID.randomUUID().toString();
    Instant now = Instant.now();
    int ttlSec = 600;

    var otp = new OtpCodeEntity();
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
    otpRepo.save(otp);

    String link = baseUrl + "/api/dev/provision/verify?token=" + token;
    String subject = "Código para criar acesso DEV";
    String html = """
      <div style="font-family:Arial,sans-serif">
        <h3>Criação de acesso DEV</h3>
        <p>Seu código é <b style="font-size:18px">%s</b> (expira em %d min).</p>
        <p>Ou clique: <a href="%s">%s</a></p>
      </div>
      """.formatted(code, ttlSec / 60, link, link);

    mailer.send(email, subject, html);

    var ed = new EmailDelivery();
    ed.setPurpose("DEV_CREATE_CODE");
    ed.setDestination(email);
    ed.setProvider("SMTP");
    ed.setStatus("SENT");
    ed.setAttempts(1);
    ed.setMessageId(null); // ou "n/a"
    ed.setPayloadJson("""
      {"subject":"%s","link":"%s","deliveryId":"%s"}
      """.formatted(subject, link, deliveryId));
    emailDeliveryRepo.save(ed);

    return deliveryId;
  }

  /**
   * Verifica o token e cria/eleva o usuário para ROLE_DEVELOPER.
   * - Se o usuário já existe, CPF/NOME são ignorados.
   * - Se não existe, CPF e NOME são obrigatórios.
   */
  @Transactional
  public void verifyByToken(String token, String optionalPassword, String cpfIfNew, String nomeIfNew) {
    var otp = otpRepo.findByVerificationToken(token)
        .orElseThrow(() -> new IllegalArgumentException("token inválido"));

    Instant now = Instant.now();
    if (!"PENDING".equalsIgnoreCase(otp.getStatus()) || now.isAfter(otp.getExpiresAt())) {
      throw new IllegalStateException("código expirado ou já utilizado");
    }

    otp.setStatus("VERIFIED");
    otp.setVerifiedAt(now);
    otp.setConsumedAt(now);
    otpRepo.save(otp);

    String email = otp.getDestination();

    Optional<UsuarioEntity> existing = usuarioRepo.findByEmailOrCpf(email);

    UsuarioEntity user;
    if (existing.isPresent()) {
      user = existing.get();
    } else {
      if (cpfIfNew == null || cpfIfNew.isBlank()) {
        throw new IllegalArgumentException("CPF é obrigatório para criação do usuário.");
      }
      if (nomeIfNew == null || nomeIfNew.isBlank()) {
        throw new IllegalArgumentException("Nome é obrigatório para criação do usuário.");
      }
      user = new UsuarioEntity();
      user.setEmail(email);
      user.setCpf(cpfIfNew);
      user.setNome(nomeIfNew);
    }

    if (optionalPassword != null && !optionalPassword.isBlank()) {
      user.setSenha(encoder.encode(optionalPassword));
    } else if (user.getSenha() == null || user.getSenha().isBlank()) {
      user.setSenha(encoder.encode(UUID.randomUUID().toString()));
    }

    // adiciona papel como entidade
    user.addRole(Role.of("ROLE_DEVELOPER"));
    usuarioRepo.save(user);
  }

  /* ========= helpers ========= */
  private static String randomCode6() {
    return String.format("%06d", RNG.nextInt(1_000_000));
  }
  private static byte[] randomSalt() {
    byte[] salt = new byte[16]; RNG.nextBytes(salt); return salt;
  }
  private static String hashCode(String code, byte[] salt) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      md.update(salt);
      return Base64.getEncoder().encodeToString(md.digest(code.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) { throw new IllegalStateException("hash failure", e); }
  }
}
