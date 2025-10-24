/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"})
public class PasswordResetApiController {
    private final PasswordResetService resetService;

    public PasswordResetApiController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping(value={"/esqueci-senha"})
    public ResponseEntity<ApiStatus> esqueciSenha(@RequestBody EsqueciSenhaRequest body) {
        String id;
        String string = id = body == null || body.emailOuCpf == null ? "" : body.emailOuCpf.trim();
        if (!id.isEmpty()) {
            this.resetService.solicitarResetPorEmailOuCpf(id);
        }
        return ResponseEntity.ok((Object)new ApiStatus("ok"));
    }

    @GetMapping(value={"/validar-token"})
    public ResponseEntity<ValidarTokenResponse> validarToken(@RequestParam String token) {
        boolean valido = this.resetService.validarToken(token).isPresent();
        return ResponseEntity.ok((Object)new ValidarTokenResponse(valido));
    }

    @PostMapping(value={"/resetar-senha"})
    public ResponseEntity<ApiStatus> resetarSenha(@RequestBody ResetarSenhaRequest body) {
        if (body == null || PasswordResetApiController.isBlank(body.token) || PasswordResetApiController.isBlank(body.novaSenha)) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body((Object)new ApiStatus("dados_invalidos"));
        }
        boolean ok = this.resetService.aplicarNovaSenha(body.token.trim(), body.novaSenha);
        if (!ok) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body((Object)new ApiStatus("token_invalido_ou_expirado"));
        }
        return ResponseEntity.ok((Object)new ApiStatus("alterada"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    public static class EsqueciSenhaRequest {
        @NotBlank
        public String emailOuCpf;

        public String getEmailOuCpf() {
            return this.emailOuCpf;
        }

        public void setEmailOuCpf(String emailOuCpf) {
            this.emailOuCpf = emailOuCpf;
        }
    }

    public static class ApiStatus {
        public String status;

        public ApiStatus() {
        }

        public ApiStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ValidarTokenResponse {
        public boolean valido;

        public ValidarTokenResponse() {
        }

        public ValidarTokenResponse(boolean valido) {
            this.valido = valido;
        }

        public boolean isValido() {
            return this.valido;
        }

        public void setValido(boolean valido) {
            this.valido = valido;
        }
    }

    public static class ResetarSenhaRequest {
        @NotBlank
        public String token;
        @NotBlank
        public String novaSenha;

        public String getToken() {
            return this.token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNovaSenha() {
            return this.novaSenha;
        }

        public void setNovaSenha(String novaSenha) {
            this.novaSenha = novaSenha;
        }
    }
}

