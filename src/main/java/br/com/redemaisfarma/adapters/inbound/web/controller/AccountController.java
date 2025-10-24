/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.springframework.data.util.Pair
 *  org.springframework.security.core.Authentication
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.core.account.UserAccountService;
import br.com.redemaisfarma.application.dto.request.ChangePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/cliente"})
public class AccountController {
    private final UserAccountService accountService;

    public AccountController(UserAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(value={"/senha"})
    public String formAlterarSenha(Model model) {
        model.addAttribute("active", (Object)"cliente");
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", (Object)new ChangePasswordRequest());
        }
        return "pages/cliente/alterar-senha";
    }

    @PostMapping(value={"/senha"})
    public String alterarSenha(@Valid @ModelAttribute(value="form") ChangePasswordRequest form, BindingResult result, Authentication auth, RedirectAttributes ra) {
        if (!form.getNovaSenha().equals(form.getConfirmarNovaSenha())) {
            result.rejectValue("confirmarNovaSenha", "password.confirm.mismatch", "As senhas n\u00e3o conferem.");
        }
        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", (Object)result);
            ra.addFlashAttribute("form", (Object)form);
            return "redirect:/cliente/senha";
        }
        Pair<Long, String> user = this.extrairUsuario(auth);
        try {
            this.accountService.changePassword((Long)user.getFirst(), form.getSenhaAtual(), form.getNovaSenha());
            ra.addFlashAttribute("infoMessage", (Object)"Senha alterada com sucesso.");
        }
        catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", (Object)e.getMessage());
        }
        return "redirect:/cliente/senha";
    }

    private Pair<Long, String> extrairUsuario(Authentication auth) {
        throw new UnsupportedOperationException("Implemente a leitura do ID do usu\u00e1rio logado.");
    }
}

