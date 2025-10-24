/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.ResponseBody
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.dto.ResetSenhaForm;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/auth"})
public class AuthController {
    private final PasswordResetService resetService;

    public AuthController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping(value={"/esqueci-senha"})
    public String esqueciSenhaForm() {
        return "auth/esqueci-senha";
    }

    @PostMapping(value={"/esqueci-senha"})
    public String esqueciSenhaSubmit(@RequestParam String identificador, RedirectAttributes ra) {
        String id;
        String string = id = identificador == null ? "" : identificador.trim();
        if (!id.isEmpty()) {
            this.resetService.solicitarResetPorEmailOuCpf(id);
        }
        ra.addFlashAttribute("infoMessage", (Object)"Se existir uma conta, enviaremos um e-mail com instru\u00e7\u00f5es.");
        return "redirect:/login";
    }

    @GetMapping(value={"/resetar-senha"})
    public String resetarSenhaForm(@RequestParam String token, Model model) {
        Optional<UsuarioEntity> userOpt = this.resetService.validarToken(token);
        if (userOpt.isEmpty()) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", (Object)"Link inv\u00e1lido ou expirado.");
            return "auth/resetar-senha";
        }
        ResetSenhaForm form = new ResetSenhaForm();
        form.setToken(token);
        model.addAttribute("form", (Object)form);
        return "auth/resetar-senha";
    }

    @PostMapping(value={"/resetar-senha"})
    public String resetarSenhaSubmit(@Valid @ModelAttribute(value="form") ResetSenhaForm form, BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            return "auth/resetar-senha";
        }
        if (!form.getNovaSenha().equals(form.getConfirmarSenha())) {
            model.addAttribute("errorMessage", (Object)"As senhas n\u00e3o coincidem.");
            return "auth/resetar-senha";
        }
        boolean ok = this.resetService.aplicarNovaSenha(form.getToken(), form.getNovaSenha());
        if (!ok) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", (Object)"Link inv\u00e1lido ou expirado.");
            return "auth/resetar-senha";
        }
        ra.addFlashAttribute("infoMessage", (Object)"Senha alterada com sucesso. Fa\u00e7a login novamente.");
        return "redirect:/login";
    }

    @PostMapping(path={"/api/esqueci-senha"})
    @ResponseBody
    public ResponseEntity<ApiStatus> apiEsqueciSenha(@Valid @RequestBody EsqueciSenhaRequest body) {
        String id;
        String string = id = body.emailOuCpf == null ? "" : body.emailOuCpf.trim();
        if (!id.isEmpty()) {
            this.resetService.solicitarResetPorEmailOuCpf(id);
        }
        return ResponseEntity.ok((Object)new ApiStatus("ok"));
    }

    @GetMapping(path={"/api/validar-token"})
    @ResponseBody
    public ResponseEntity<ValidarTokenResponse> apiValidarToken(@RequestParam String token) {
        boolean valido = this.resetService.validarToken(token).isPresent();
        return ResponseEntity.ok((Object)new ValidarTokenResponse(valido));
    }

    @PostMapping(path={"/api/resetar-senha"})
    @ResponseBody
    public ResponseEntity<?> apiResetarSenha(@Valid @RequestBody ResetarSenhaRequest body) {
        boolean ok = this.resetService.aplicarNovaSenha(body.token, body.novaSenha);
        if (!ok) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body((Object)new ApiStatus("token_invalido_ou_expirado"));
        }
        return ResponseEntity.ok((Object)new ApiStatus("alterada"));
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

