package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth/email-claim", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AuthEmailClaimController {

    private final OtpServicePort otp;
    private final UsuarioRepository usuarios;

    public AuthEmailClaimController(OtpServicePort otp, UsuarioRepository usuarios) {
        this.otp = otp;
        this.usuarios = usuarios;
    }

    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StartRes> start(@RequestBody @Valid StartReq req) {
        OtpServicePort.StartResult s = otp.start("email", req.email(), req.previousDeliveryId());
        boolean exists = usuarios.existsByEmailIgnoreCase(req.email());
        StartRes body = new StartRes(s.deliveryId(), s.maskedDestino(), s.cooldownSec(), s.ttlSeconds(), exists, s.demoCode());
        return ResponseEntity.ok(body);
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyReq req) {
        try {
            String token = otp.verify(req.deliveryId(), req.code());
            boolean exists = usuarios.existsByEmailIgnoreCase(req.email());
            return ResponseEntity.ok(new VerifyRes(token, exists));
        } catch (OtpServicePort.OtpException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason", ex.reason(),
                    "message", ex.getMessage()
            ));
        }
    }

    // ---- DTOs (records) ----
    public record StartReq(@NotBlank @Email String email, String previousDeliveryId) {}
    public record StartRes(String deliveryId, String maskedDestino, int cooldownSec, int ttlSeconds, boolean userExists, String demoCode) {}
    public record VerifyReq(@NotBlank String deliveryId, @NotBlank String code, @NotBlank @Email String email) {}
    public record VerifyRes(String token, boolean userExists) {}
}
