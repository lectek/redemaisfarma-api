/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PasswordResetTokenEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PasswordResetTokenRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderAdapter mailAdapter;
    private final SecureRandom random = new SecureRandom();
    private final String baseUrl;

    public PasswordResetService(UsuarioRepository usuarioRepository, PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder, MailSenderAdapter mailAdapter, @Value(value="${app.web.base-url:http://localhost:18090}") String baseUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailAdapter = mailAdapter;
        this.baseUrl = PasswordResetService.normalizeBaseUrl(baseUrl);
    }

    @Transactional
    public void solicitarResetPorEmailOuCpf(String emailOuCpf) {
        this.tokenRepository.deleteByUsadoIsTrueOrExpiraEmBefore(LocalDateTime.now().minusDays(1L));
        if (emailOuCpf == null || emailOuCpf.isBlank()) {
            return;
        }
        String raw = emailOuCpf.trim();
        String email = raw.contains("@") ? raw.toLowerCase() : null;
        String cpf = raw.contains("@") ? null : raw.replaceAll("\\D", "");
        Optional<UsuarioEntity> opt = email != null ? this.usuarioRepository.findByEmailOrCpf(email) : this.usuarioRepository.findByEmailOrCpf(cpf);
        Optional<UsuarioEntity> optional = opt;
        if (opt.isEmpty()) {
            log.debug("Solicita\u00e7\u00e3o de reset para '{}': usu\u00e1rio n\u00e3o encontrado (silencioso).", (Object)raw);
            return;
        }
        UsuarioEntity user = opt.get();
        this.tokenRepository.deleteByUsuarioId(user.getId());
        String token = this.gerarToken();
        PasswordResetTokenEntity prt = new PasswordResetTokenEntity();
        prt.setToken(token);
        prt.setUsuarioId(user.getId());
        prt.setExpiraEm(LocalDateTime.now().plusMinutes(30L));
        this.tokenRepository.save(prt);
        String link = this.baseUrl + "/cliente/auth/resetar-senha?token=" + token;
        String assunto = "Redefini\u00e7\u00e3o de senha - RedeMaisFarma";
        String html = this.buildEmailHtml(user.getNome(), link);
        this.mailAdapter.send(user.getEmail(), assunto, html, null);
        log.info("Link de reset emitido para usu\u00e1rio id={} (expira em 30 min).", (Object)user.getId());
    }

    public Optional<UsuarioEntity> validarToken(String token) {
        return this.tokenRepository.findByToken(token).filter(t -> !t.isUsado()).filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now())).flatMap(t -> this.usuarioRepository.findById(t.getUsuarioId()));
    }

    @Transactional
    public boolean aplicarNovaSenha(String token, String novaSenha) {
        Optional<PasswordResetTokenEntity> opt = this.tokenRepository.findByToken(token).filter(t -> !t.isUsado()).filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now()));
        if (opt.isEmpty()) {
            return false;
        }
        PasswordResetTokenEntity t2 = opt.get();
        UsuarioEntity u = this.usuarioRepository.findById(t2.getUsuarioId()).orElse(null);
        if (u == null) {
            return false;
        }
        u.setSenha(this.passwordEncoder.encode((CharSequence)novaSenha));
        this.usuarioRepository.save(u);
        t2.setUsado(true);
        t2.setUsadoEm(LocalDateTime.now());
        this.tokenRepository.save(t2);
        log.info("Senha redefinida para usu\u00e1rio id={} via token.", (Object)u.getId());
        return true;
    }

    private String gerarToken() {
        byte[] bytes = new byte[48];
        this.random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:18090";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String buildEmailHtml(String nome, String link) {
        return "<!doctype html>\n<html lang=\"pt-BR\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n  <title>Redefini\u00e7\u00e3o de senha</title>\n  <style>\n    body{font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:0;padding:24px;background:#f6f7f9;color:#222}\n    .card{max-width:560px;margin:0 auto;background:#fff;border:1px solid #e6e8eb;border-radius:12px;padding:24px}\n    .btn{display:inline-block;padding:12px 18px;border-radius:8px;text-decoration:none}\n    .btn-primary{background:#2563eb;color:#fff}\n    .muted{color:#6b7280;font-size:12px}\n  </style>\n</head>\n<body>\n  <div class=\"card\">\n    <p>Ol\u00e1, <strong>%s</strong>!</p>\n    <p>Recebemos um pedido para redefinir sua senha na <strong>RedeMaisFarma</strong>.</p>\n    <p>Para continuar, clique no bot\u00e3o abaixo (o link \u00e9 v\u00e1lido por <strong>30 minutos</strong>):</p>\n    <p><a class=\"btn btn-primary\" href=\"%s\" target=\"_blank\" rel=\"noopener\">Redefinir minha senha</a></p>\n    <p class=\"muted\">Se voc\u00ea n\u00e3o solicitou, pode ignorar este e-mail.</p>\n  </div>\n</body>\n</html>\n".formatted(nome, link);
    }
}

