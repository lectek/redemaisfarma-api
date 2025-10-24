/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.http.ResponseEntity
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth/email-claim"}, produces={"application/json"})
@Validated
public class AuthEmailClaimController {
    private final OtpServicePort otp;
    private final UsuarioRepository usuarios;

    public AuthEmailClaimController(OtpServicePort otp, UsuarioRepository usuarios) {
        this.otp = otp;
        this.usuarios = usuarios;
    }

    @PostMapping(value={"/start"}, consumes={"application/json"})
    public ResponseEntity<StartRes> start(@RequestBody @Validated StartReq req) {
        OtpServicePort.StartResult s = this.otp.start("email", req.email(), req.previousDeliveryId());
        boolean exists = this.usuarios.existsByEmailIgnoreCase(req.email());
        return ResponseEntity.ok((Object)new StartRes(s.deliveryId(), s.maskedDestino(), s.cooldownSec(), s.ttlSeconds(), exists, s.demoCode()));
    }

    @PostMapping(value={"/verify"}, consumes={"application/json"})
    public ResponseEntity<?> verify(@RequestBody @Validated VerifyReq req) {
        try {
            String token = this.otp.verify(req.deliveryId(), req.code());
            boolean exists = this.usuarios.existsByEmailIgnoreCase(req.email());
            return ResponseEntity.ok((Object)new VerifyRes(token, exists));
        }
        catch (OtpServicePort.OtpException ex) {
            return ResponseEntity.badRequest().body(Map.of("reason", ex.reason(), "message", ex.getMessage()));
        }
    }

    public record StartReq(@NotBlank @Email String email, String previousDeliveryId) {
    }

    public record StartRes(String deliveryId, String maskedDestino, int cooldownSec, int ttlSeconds, boolean userExists, String demoCode) {
    }

    public record VerifyReq(@NotBlank String deliveryId, @NotBlank String code, @NotBlank @Email String email) {
    }

    public record VerifyRes(String token, boolean userExists) {
    }
}

