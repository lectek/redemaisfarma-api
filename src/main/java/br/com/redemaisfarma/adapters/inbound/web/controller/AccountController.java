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

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.account.UserAccountService;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.dto.request.ChangePasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.Optional;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value={"/cliente"})
public class AccountController {
    private static final long AVATAR_MAX_BYTES = 2L * 1024L * 1024L;
    private final UserAccountService accountService;
    private final UsuarioRepository usuarioRepository;
    private final ImageStorageService imageStorageService;

    public AccountController(UserAccountService accountService,
                             UsuarioRepository usuarioRepository,
                             ImageStorageService imageStorageService) {
        this.accountService = accountService;
        this.usuarioRepository = usuarioRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping(value={"/senha"})
    public String formAlterarSenha(Model model) {
        model.addAttribute("active", (Object)"conta");
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", (Object)new ChangePasswordRequest());
        }
        return "pages/cliente/alterar-senha";
    }

    @GetMapping(value={"/conta"})
    public String conta(Model model, Authentication auth) {
        model.addAttribute("active", (Object)"conta");
        localizarUsuario(auth).ifPresent(usuario -> {
            model.addAttribute("avatarUrl", usuario.getAvatarUrl());
            model.addAttribute("avatarNome", usuario.getNome());
        });
        return "pages/cliente/conta";
    }

    @GetMapping(value={"/dados"})
    public String dados(Model model, Authentication auth) {
        model.addAttribute("active", (Object)"dados");
        UsuarioEntity usuario = localizarUsuario(auth).orElse(null);
        if (usuario != null) {
            model.addAttribute("avatarUrl", usuario.getAvatarUrl());
            model.addAttribute("avatarNome", usuario.getNome());
        }
        if (!model.containsAttribute("form")) {
            ClienteDadosForm form = new ClienteDadosForm();
            if (usuario != null) {
                form.setNome(usuario.getNome());
                form.setEmail(usuario.getEmail());
                form.setCpf(usuario.getCpf());
                form.setTelefone(usuario.getTelefone());
                form.setEndereco(usuario.getEndereco());
            }
            model.addAttribute("form", (Object)form);
        }
        return "pages/cliente/dados";
    }

    @PostMapping(value={"/dados"})
    public String atualizarDados(@Valid @ModelAttribute(value="form") ClienteDadosForm form, BindingResult result, Authentication auth, RedirectAttributes ra) {
        Optional<UsuarioEntity> usuarioOpt = localizarUsuario(auth);
        if (usuarioOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", (Object)"Usu\u00e1rio n\u00e3o encontrado.");
            return "redirect:/cliente/dados";
        }

        UsuarioEntity usuario = usuarioOpt.get();
        String emailNormalizado = normalizarEmail(form.getEmail());

        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            result.rejectValue("email", "email.invalid", "Informe um e-mail v\u00e1lido.");
        } else if (!emailNormalizado.equalsIgnoreCase(usuario.getEmail())) {
            usuarioRepository.findByEmailIgnoreCase(emailNormalizado).ifPresent(existente -> {
                if (!existente.getId().equals(usuario.getId())) {
                    result.rejectValue("email", "email.duplicado", "E-mail j\u00e1 cadastrado.");
                }
            });
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", (Object)result);
            ra.addFlashAttribute("form", (Object)form);
            return "redirect:/cliente/dados";
        }

        usuario.setNome(form.getNome());
        usuario.setEmail(emailNormalizado);
        usuario.setTelefone(normalizarTelefone(form.getTelefone()));
        usuario.setEndereco(normalizarEndereco(form.getEndereco()));
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("infoMessage", (Object)"Dados atualizados com sucesso.");
        return "redirect:/cliente/dados";
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

    @PostMapping(value={"/avatar"})
    public String atualizarAvatar(@RequestParam("file") MultipartFile file,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        Optional<UsuarioEntity> usuarioOpt = localizarUsuario(auth);
        if (usuarioOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", (Object)"Usu\u00e1rio n\u00e3o encontrado.");
            return "redirect:/cliente/dados";
        }
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", (Object)"Selecione uma imagem para enviar.");
            return "redirect:/cliente/dados";
        }
        if (file.getSize() > AVATAR_MAX_BYTES) {
            ra.addFlashAttribute("errorMessage", (Object)"Imagem acima de 2MB. Reduza o tamanho e tente novamente.");
            return "redirect:/cliente/dados";
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp")) {
            ra.addFlashAttribute("errorMessage", (Object)"Formato invalido. Use PNG, JPG ou WEBP.");
            return "redirect:/cliente/dados";
        }

        UsuarioEntity usuario = usuarioOpt.get();
        try {
            String url = imageStorageService.saveUserAvatar(usuario.getId(), file);
            usuario.setAvatarUrl(url);
            usuarioRepository.save(usuario);
            ra.addFlashAttribute("infoMessage", (Object)"Foto atualizada com sucesso.");
        } catch (IOException ex) {
            ra.addFlashAttribute("errorMessage", (Object)ex.getMessage());
        }
        return "redirect:/cliente/dados";
    }

    private Pair<Long, String> extrairUsuario(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new IllegalArgumentException("Usu\u00e1rio n\u00e3o encontrado."));
        return Pair.of((Long)usuario.getId(), (String)usuario.getEmail());
    }

    private Optional<UsuarioEntity> localizarUsuario(Authentication auth) {
        if (auth == null || auth.getName() == null) return Optional.empty();
        return usuarioRepository.findByEmailOrCpf(auth.getName());
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) return null;
        String trimmed = telefone.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizarEndereco(String endereco) {
        if (endereco == null) return null;
        String trimmed = endereco.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static class ClienteDadosForm {

        @NotBlank(message = "Informe seu nome.")
        @Size(max = 120, message = "O nome deve ter no m\u00e1ximo 120 caracteres.")
        private String nome;

        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "E-mail inv\u00e1lido.")
        @Size(max = 150, message = "O e-mail deve ter no m\u00e1ximo 150 caracteres.")
        private String email;

        private String cpf;

        @Size(max = 25, message = "O telefone deve ter no maximo 25 caracteres.")
        private String telefone;

        @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres.")
        private String endereco;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }

        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }

        public String getEndereco() { return endereco; }
        public void setEndereco(String endereco) { this.endereco = endereco; }
    }
}
