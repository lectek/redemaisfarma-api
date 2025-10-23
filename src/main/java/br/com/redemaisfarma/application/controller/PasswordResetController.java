// src/main/java/br/com/redemaisfarma/application/controller/PasswordResetController.java
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.dto.request.ForgotPasswordRequest;
import br.com.redemaisfarma.application.dto.request.ResetPasswordRequest;
import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller MVC para o fluxo clássico (link por e-mail) em páginas.
 * O fluxo via OTP/JS ficará exposto por endpoints REST dedicados (em outro controller),
 * assim evitamos conflitos de path com "/cliente/auth/**".
 */
@Controller
@RequestMapping("/cliente/auth")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    /** Trima strings e converte "" em null para os formulários desta página. */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // ---- Esqueci minha senha (view cliente) ----
    @GetMapping("/esqueci-senha")
    public String forgotForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ForgotPasswordRequest());
        }
        return "pages/cliente/esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String forgotSubmit(@Valid @ModelAttribute("form") ForgotPasswordRequest form,
                               BindingResult br,
                               Model model) {
        if (br.hasErrors()) {
            return "pages/cliente/esqueci-senha";
        }

        try {
            service.solicitarResetPorEmailOuCpf(form.getEmailOuCpf());
            log.info("Solicitação de reset recebida para identificador='{}' (se existir).", form.getEmailOuCpf());
        } catch (Exception ex) {
            // Não vaza detalhe para o usuário (evita enumeração de contas)
            log.warn("Falha ao solicitar reset para '{}': {}", form.getEmailOuCpf(), ex.getMessage());
        }

        model.addAttribute("infoMessage",
            "Se encontrarmos sua conta, enviaremos um link de redefinição para o e-mail cadastrado.");
        model.addAttribute("form", new ForgotPasswordRequest()); // limpa o form
        return "pages/cliente/esqueci-senha";
    }

    // ---- Resetar com token (view cliente) ----
    @GetMapping("/resetar-senha")
    public String resetForm(@RequestParam("token") String token, Model model) {
        var valido = service.validarToken(token).isPresent();
        if (!valido) {
            model.addAttribute("errorMessage", "Link inválido ou expirado. Solicite novamente.");
            return "pages/cliente/resetar-senha";
        }
        ResetPasswordRequest form = new ResetPasswordRequest();
        form.setToken(token);
        model.addAttribute("form", form);
        return "pages/cliente/resetar-senha";
    }

    @PostMapping("/resetar-senha")
    public String resetSubmit(@Valid @ModelAttribute("form") ResetPasswordRequest form,
                              BindingResult br,
                              Model model) {
        if (form.getNovaSenha() != null && form.getConfirmarSenha() != null
                && !form.getNovaSenha().equals(form.getConfirmarSenha())) {
            br.rejectValue("confirmarSenha", "mismatch", "As senhas não conferem.");
        }
        if (br.hasErrors()) {
            return "pages/cliente/resetar-senha";
        }

        boolean ok;
        try {
            ok = service.aplicarNovaSenha(form.getToken(), form.getNovaSenha());
        } catch (Exception ex) {
            log.error("Erro ao aplicar nova senha via token: {}", ex.getMessage(), ex);
            ok = false;
        }

        if (!ok) {
            model.addAttribute("errorMessage", "Link inválido ou expirado. Solicite novamente.");
            return "pages/cliente/resetar-senha";
        }

        model.addAttribute("successMessage", "Senha redefinida com sucesso! Você já pode entrar.");
        // Mantém a mesma página com mensagem de sucesso — UX simples
        return "pages/cliente/resetar-senha";
    }
}
