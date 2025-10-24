/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpSession
 *  jakarta.validation.Valid
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.MessageSource
 *  org.springframework.security.web.csrf.CsrfToken
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.SessionAttributes
 *  org.springframework.web.bind.support.SessionStatus
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.service.AuthService;
import br.com.redemaisfarma.application.session.SessaoCliente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes(value={"sessaoCliente"})
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final AuthService authService;
    private final MessageSource messageSource;

    public LoginController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @ModelAttribute
    public void exposeCsrf(HttpServletRequest request, Model model) {
        CsrfToken csrf = (CsrfToken)request.getAttribute("_csrf");
        if (csrf != null) {
            model.addAttribute("_csrf", (Object)csrf);
        }
    }

    @ModelAttribute(value="loginForm")
    public LoginRequest loginForm() {
        return new LoginRequest();
    }

    @GetMapping(value={"/login"})
    public String showLoginForm(@RequestParam(value="logout", required=false) String logout, Model model, Locale locale) {
        if ("true".equals(logout)) {
            model.addAttribute("infoMessage", (Object)this.messageSource.getMessage("login.info.loggedOut", null, locale));
        }
        return "pages/login";
    }

    @PostMapping(value={"/login"})
    public String processLogin(@ModelAttribute(value="loginForm") @Valid LoginRequest form, BindingResult bindingResult, Model model, HttpServletRequest request, Locale locale) {
        if (bindingResult.hasErrors()) {
            logger.warn("Erros de valida\u00e7\u00e3o: {}", (Object)bindingResult.getFieldErrors());
            model.addAttribute("loginError", (Object)this.messageSource.getMessage("login.error.validation", null, locale));
            return "pages/login";
        }
        String identifier = form.getUsuario().trim();
        String password = form.getSenha();
        if (this.authService.isBlocked(identifier)) {
            model.addAttribute("loginError", (Object)this.messageSource.getMessage("login.error.blocked", null, locale));
            return "pages/login";
        }
        try {
            AuthResponse response = this.authService.authenticate(identifier, password);
            SessaoCliente sessao = new SessaoCliente(response.getUserId(), response.getUsername(), response.getEmail(), LocalDateTime.now());
            model.addAttribute("sessaoCliente", (Object)sessao);
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("sessaoCliente", (Object)sessao);
            return "redirect:/painel";
        }
        catch (InvalidCredentialsException ex) {
            logger.info("Falha de login: {}", (Object)identifier);
            this.authService.registerFailedAttempt(identifier);
            model.addAttribute("loginError", (Object)this.messageSource.getMessage("login.error.invalid", null, locale));
            return "pages/login";
        }
        catch (Exception ex) {
            logger.error("Erro durante o login", (Throwable)ex);
            model.addAttribute("loginError", (Object)this.messageSource.getMessage("login.error.unexpected", null, locale));
            return "pages/login";
        }
    }

    @GetMapping(value={"/logout"})
    public String logout(SessionStatus status, HttpSession session) {
        status.setComplete();
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}

