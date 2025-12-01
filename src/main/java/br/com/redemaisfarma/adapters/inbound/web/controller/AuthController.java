package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.dto.ResetSenhaForm;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final PasswordResetService resetService;

    public AuthController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    // ---------------------- Views (MVC) ----------------------

    @GetMapping("/esqueci-senha")
    public String esqueciSenhaForm() {
        return "auth/esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String esqueciSenhaSubmit(@RequestParam String identificador, RedirectAttributes ra) {
        String id = identificador == null ? "" : identificador.trim();
        if (!id.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(id);
        }
        ra.addFlashAttribute("infoMessage", "Se existir uma conta, enviaremos um e-mail com instruções.");
        return "redirect:/login";
    }

    @GetMapping("/resetar-senha")
    public String resetarSenhaForm(@RequestParam String token, Model model) {
        Optional<UsuarioEntity> userOpt = resetService.validarToken(token);
        if (userOpt.isEmpty()) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", "Link inválido ou expirado.");
            return "auth/resetar-senha";
        }
        ResetSenhaForm form = new ResetSenhaForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "auth/resetar-senha";
    }

    @PostMapping("/resetar-senha")
    public String resetarSenhaSubmit(
            @Valid @ModelAttribute("form") ResetSenhaForm form,
            BindingResult br,
            RedirectAttributes ra,
            Model model) {

        if (br.hasErrors()) {
            return "auth/resetar-senha";
        }
        if (!form.getNovaSenha().equals(form.getConfirmarSenha())) {
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "auth/resetar-senha";
        }
        boolean ok = resetService.aplicarNovaSenha(form.getToken(), form.getNovaSenha());
        if (!ok) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", "Link inválido ou expirado.");
            return "auth/resetar-senha";
        }
        ra.addFlashAttribute("infoMessage", "Senha alterada com sucesso. Faça login novamente.");
        return "redirect:/login";
    }

    // ---------------------- API (JSON) ----------------------

    @PostMapping(path = "/api/esqueci-senha")
    @ResponseBody
    public ResponseEntity<ApiStatus> apiEsqueciSenha(@Valid @RequestBody EsqueciSenhaRequest body) {
        String id = body.emailOuCpf == null ? "" : body.emailOuCpf.trim();
        if (!id.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(id);
        }
        return ResponseEntity.ok(new ApiStatus("ok"));
    }

    @GetMapping(path = "/api/validar-token")
    @ResponseBody
    public ResponseEntity<ValidarTokenResponse> apiValidarToken(@RequestParam String token) {
        boolean valido = resetService.validarToken(token).isPresent();
        return ResponseEntity.ok(new ValidarTokenResponse(valido));
    }

    @PostMapping(path = "/api/resetar-senha")
    @ResponseBody
    public ResponseEntity<ApiStatus> apiResetarSenha(@Valid @RequestBody ResetarSenhaRequest body) {
        boolean ok = resetService.aplicarNovaSenha(body.token, body.novaSenha);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("token_invalido_ou_expirado"));
        }
        return ResponseEntity.ok(new ApiStatus("alterada"));
    }

    // ---------------------- DTOs ----------------------

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
