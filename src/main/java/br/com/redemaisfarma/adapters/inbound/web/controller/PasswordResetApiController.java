package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetApiController {

    private final PasswordResetService resetService;

    public PasswordResetApiController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<ApiStatus> esqueciSenha(@RequestBody EsqueciSenhaRequest body) {
        String id = (body == null || body.emailOuCpf == null) ? "" : body.emailOuCpf.trim();
        if (!id.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(id);
        }
        return ResponseEntity.ok(new ApiStatus("ok"));
    }

    @GetMapping("/validar-token")
    public ResponseEntity<ValidarTokenResponse> validarToken(@RequestParam("token") String token) {
        boolean valido = resetService.validarToken(token).isPresent();
        return ResponseEntity.ok(new ValidarTokenResponse(valido));
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<ApiStatus> resetarSenha(@RequestBody ResetarSenhaRequest body) {
        if (body == null || isBlank(body.token) || isBlank(body.novaSenha)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("dados_invalidos"));
        }
        boolean ok = resetService.aplicarNovaSenha(body.token.trim(), body.novaSenha);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("token_invalido_ou_expirado"));
        }
        return ResponseEntity.ok(new ApiStatus("alterada"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ===== DTOs =====

    public static class EsqueciSenhaRequest {
        @NotBlank
        public String emailOuCpf;

        public String getEmailOuCpf() { return emailOuCpf; }
        public void setEmailOuCpf(String emailOuCpf) { this.emailOuCpf = emailOuCpf; }
    }

    public static class ApiStatus {
        public String status;

        public ApiStatus() {}
        public ApiStatus(String status) { this.status = status; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ValidarTokenResponse {
        public boolean valido;

        public ValidarTokenResponse() {}
        public ValidarTokenResponse(boolean valido) { this.valido = valido; }

        public boolean isValido() { return valido; }
        public void setValido(boolean valido) { this.valido = valido; }
    }

    public static class ResetarSenhaRequest {
        @NotBlank
        public String token;
        @NotBlank
        public String novaSenha;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getNovaSenha() { return novaSenha; }
        public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    }
}
