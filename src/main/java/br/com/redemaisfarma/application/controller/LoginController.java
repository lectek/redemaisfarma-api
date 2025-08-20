package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.AuthService;
import br.com.redemaisfarma.application.dto.request.LoginRequestDTO;
import br.com.redemaisfarma.application.session.SessaoCliente;
import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.security.web.csrf.CsrfToken;

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@SessionAttributes("sessaoCliente")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final AuthService authService;
    private final MessageSource messageSource;

    public LoginController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @ModelAttribute("loginForm")
    public LoginRequestDTO loginForm() {
        return new LoginRequestDTO();
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "logout", required = false) String logout, Model model,
            HttpServletRequest request, Locale locale) {

        if ("true".equals(logout)) {
            model.addAttribute("infoMessage", messageSource.getMessage("login.info.loggedOut", null, locale));
        }

        CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrf);

        return "pages/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("loginForm") @Valid LoginRequestDTO form, BindingResult bindingResult,
            Model model, HttpServletRequest request, Locale locale) {

        if (bindingResult.hasErrors()) {
            logger.warn("Erros de validaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o: {}", bindingResult.getFieldErrors());
            model.addAttribute("loginError", messageSource.getMessage("login.error.validation", null, locale));
            return "pages/login";
        }

        String identifier = form.getUsuario().trim();
        String password = form.getSenha();

        if (authService.isBlocked(identifier)) {
            model.addAttribute("loginError", messageSource.getMessage("login.error.blocked", null, locale));
            return "pages/login";
        }

        try {
            AuthResponse response = authService.authenticate(identifier, password);

            SessaoCliente sessao = new SessaoCliente(response.getUserId(), response.getUsername(), response.getEmail(),
                    LocalDateTime.now());

            model.addAttribute("sessaoCliente", sessao);
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("sessaoCliente", sessao);

            return "redirect:/painel";

        } catch (InvalidCredentialsException ex) {
            logger.info("Falha de login: {}", identifier);
            authService.registerFailedAttempt(identifier);
            model.addAttribute("loginError", messageSource.getMessage("login.error.invalid", null, locale));
            return "pages/login";

        } catch (Exception ex) {
            logger.error("Erro durante o login", ex);
            model.addAttribute("loginError", messageSource.getMessage("login.error.unexpected", null, locale));
            return "pages/login";
        }
    }

    @GetMapping("/logout")
    public String logout(SessionStatus status, HttpSession session) {
        status.setComplete();
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
