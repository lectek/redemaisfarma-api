/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"}, produces={"application/json"})
@Validated
public class AuthOtpActionsController {
    private final OtpServicePort otp;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final ClienteService clienteService;

    public AuthOtpActionsController(OtpServicePort otp, UsuarioRepository usuarios, PasswordEncoder encoder, ClienteService clienteService) {
        this.otp = otp;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.clienteService = clienteService;
    }

    @PostMapping(value={"/password/reset-otp"}, consumes={"application/json"})
    public ResponseEntity<?> resetByOtp(@RequestBody @Validated ResetReq req) {
        if (!this.otp.consumeTokenForDestino(req.token(), req.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token inv\u00e1lido, expirado ou n\u00e3o pertence a este e-mail."));
        }
        UsuarioEntity user = this.usuarios.findByEmailIgnoreCase(req.email()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usu\u00e1rio n\u00e3o encontrado para este e-mail."));
        }
        user.setSenha(this.encoder.encode((CharSequence)req.novaSenha()));
        this.usuarios.save(user);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida."));
    }

    @PostMapping(value={"/register/complete-otp"}, consumes={"application/json"})
    public ResponseEntity<?> registerByOtp(@RequestBody @Validated RegisterReq req) {
        if (!this.otp.consumeTokenForDestino(req.token(), req.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token inv\u00e1lido, expirado ou n\u00e3o pertence a este e-mail."));
        }
        if (this.usuarios.existsByEmailIgnoreCase(req.email())) {
            return ResponseEntity.status((int)409).body(Map.of("message", "E-mail j\u00e1 cadastrado."));
        }
        Cliente c = new Cliente();
        c.setNome(req.nome());
        c.setEmail(req.email().toLowerCase());
        c.setSenha(this.encoder.encode((CharSequence)req.senha()));
        c.setAtivo(true);
        c.setCpf(null);
        c.setTelefone(null);
        c.setDataDeNascimento(null);
        this.clienteService.create(c);
        return ResponseEntity.ok(Map.of("message", "Conta criada com sucesso.", "email", req.email()));
    }

    public record ResetReq(@NotBlank String token, @NotBlank @Email String email, @NotBlank @Size(min=8, max=128) @NotBlank @Size(min=8, max=128) String novaSenha) {
    }

    public record RegisterReq(@NotBlank String token, @NotBlank @Email String email, @NotBlank String nome, @NotBlank @Size(min=8, max=128) @NotBlank @Size(min=8, max=128) String senha) {
    }
}

