package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.dto.request.ChangePasswordRequest;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class ChangePasswordController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordController(UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** GET: exibe o formulário (templates/pages/cliente/mudar-senha.html) */
    @GetMapping("/mudar-senha")
    public String form(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ChangePasswordRequest());
        }
        return "pages/cliente/mudar-senha";
    }

    /** POST: processa a troca de senha e força novo login */
    @PostMapping("/mudar-senha")
    public String submit(@AuthenticationPrincipal UserDetails me,
                         @Valid @ModelAttribute("form") ChangePasswordRequest form,
                         BindingResult br,
                         RedirectAttributes ra,
                         HttpServletRequest req,
                         HttpServletResponse res) {

        // 0) Sessão obrigatória
        if (me == null) {
            ra.addFlashAttribute("loginError", "Sua sessão expirou. Faça login novamente.");
            return "redirect:/login";
        }

        // 1) Validações do formulário
        if (br.hasErrors()) return "pages/cliente/mudar-senha";

        final String nova = safe(form.getNovaSenha());
        final String conf = safe(form.getConfirmarNovaSenha());
        final String atual = safe(form.getSenhaAtual());

        if (!nova.equals(conf)) {
            br.rejectValue("confirmarNovaSenha", "mismatch", "As senhas não conferem.");
            return "pages/cliente/mudar-senha";
        }

        // 2) Localiza usuário por e-mail (CI) ou CPF (11 dígitos)
        final String login = safe(me.getUsername());
        UsuarioEntity u = localizarPorEmailOuCpf(login)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado para: " + login));

        // 3) Confere senha atual
        if (!passwordEncoder.matches(atual, u.getSenha())) {
            br.rejectValue("senhaAtual", "invalid", "Senha atual incorreta.");
            return "pages/cliente/mudar-senha";
        }

        // 4) Evita reutilização imediata
        if (passwordEncoder.matches(nova, u.getSenha())) {
            br.rejectValue("novaSenha", "reused", "A nova senha não pode ser igual à senha atual.");
            return "pages/cliente/mudar-senha";
        }

        // 5) Atualiza a senha (em transação)
        u.setSenha(passwordEncoder.encode(nova));
        usuarioRepository.save(u);

        // 6) Força novo login por segurança
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(req, res, auth);

        ra.addFlashAttribute("infoMessage", "Senha alterada com sucesso. Faça login novamente.");
        return "redirect:/login";
    }

    // ===== helpers =====

    private Optional<UsuarioEntity> localizarPorEmailOuCpf(String login) {
        if (login == null || login.isBlank()) return Optional.empty();
        String ident = login.trim();

        if (ident.contains("@")) {
            // requer método no repository: Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
            return usuarioRepository.findByEmailIgnoreCase(ident.toLowerCase());
        }

        String cpfDigits = ident.replaceAll("\\D", "");
        if (cpfDigits.length() == 11) {
            // requer método no repository: Optional<UsuarioEntity> findByCpf(String cpf);
            return usuarioRepository.findByCpf(cpfDigits);
        }

        // fallback se você tiver uma query combinada (senão pode remover esta linha)
        return usuarioRepository.findByEmailOrCpf(ident);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
