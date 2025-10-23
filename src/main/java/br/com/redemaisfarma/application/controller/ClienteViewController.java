package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/clientes")
public class ClienteViewController {

    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;
    private final OtpServicePort otpService;

    @Value("${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    /** Quando true (ex.: em DEV/Docker), ignora validações/OTP para facilitar fluxo. */
    @Value("${app.signup.relaxed:false}")
    private boolean relaxedSignup;

    public ClienteViewController(ClienteService clienteService,
                                 PasswordEncoder passwordEncoder,
                                 OtpServicePort otpService) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    /** Trima strings e converte "" para null. */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** GET /clientes/cadastro -> templates/pages/cliente/cadastro-cliente.html */
    @GetMapping("/cadastro")
    public String showForm(Model model) {
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", new ClienteForm());
        }
        model.addAttribute("oauth2Enabled", oauth2Enabled);
        model.addAttribute("relaxedSignup", relaxedSignup); // para o HTML/JS
        return "pages/cliente/cadastro-cliente";
    }

    /** POST /clientes (submit do formulário) */
    @PostMapping
    public String processForm(@Valid @ModelAttribute("cliente") ClienteForm dto,
                              BindingResult binding,
                              @RequestParam(value = "otpToken", required = false) String otpToken,
                              RedirectAttributes ra,
                              Model model) {

        // senha = confirmarSenha
        if (dto.getSenha() != null && dto.getConfirmarSenha() != null &&
                !dto.getSenha().equals(dto.getConfirmarSenha())) {
            binding.rejectValue("confirmarSenha", "senha.nao.confere", "As senhas não conferem.");
        }

        // Modo relaxado: ignora erros e preenche defaults seguros
        if (binding.hasErrors() && relaxedSignup) {
            binding.getAllErrors().clear();
            if (dto.getNome() == null) dto.setNome("Usuário");
            if (dto.getEmail() == null) dto.setEmail("dev@example.com");
            if (dto.getSenha() == null) dto.setSenha("Aa123456!");
            if (dto.getConfirmarSenha() == null) dto.setConfirmarSenha(dto.getSenha());
            if (dto.getCpf() == null) dto.setCpf("000.000.000-00");
        }

        // Em modo normal, volta para o formulário se houver erros
        if (binding.hasErrors() && !relaxedSignup) {
            model.addAttribute("oauth2Enabled", oauth2Enabled);
            model.addAttribute("relaxedSignup", relaxedSignup);
            return "pages/cliente/cadastro-cliente";
        }

        // OTP obrigatório em modo normal
        if (!relaxedSignup) {
            if (otpToken == null || otpToken.isBlank() || !otpService.consumeToken(otpToken)) {
                binding.reject("otp.invalido", "Código de verificação inválido ou expirado.");
                model.addAttribute("oauth2Enabled", oauth2Enabled);
                model.addAttribute("relaxedSignup", relaxedSignup);
                return "pages/cliente/cadastro-cliente";
            }
        }

        // Normalizações
        String email = dto.getEmail() != null ? dto.getEmail().toLowerCase() : null;
        String cpfSomenteDigitos = dto.getCpf() != null ? dto.getCpf().replaceAll("\\D", "") : null;

        // Mapeia para domínio
        Cliente entity = new Cliente();
        entity.setNome(dto.getNome());
        entity.setEmail(email);
        entity.setTelefone(dto.getTelefone());
        entity.setCpf(cpfSomenteDigitos);
        entity.setDataDeNascimento(dto.getDataDeNascimento());
        entity.setAtivo(true);
        entity.setSenha(passwordEncoder.encode(dto.getSenha())); // bcrypt

        try {
            clienteService.create(entity);
        } catch (DataIntegrityViolationException dive) {
            // Caso haja UNIQUE no banco para email/cpf
            binding.reject("duplicado", "E-mail ou CPF já cadastrado.");
            model.addAttribute("oauth2Enabled", oauth2Enabled);
            model.addAttribute("relaxedSignup", relaxedSignup);
            return "pages/cliente/cadastro-cliente";
        }

        ra.addFlashAttribute("infoMessage", "Conta criada com sucesso! Você já pode fazer login.");
        return "redirect:/login";
    }

    /** DTO do formulário (compatível com cadastro-cliente.html). */
    public static class ClienteForm {

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        private String nome;

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
        private String email;

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        private String senha;

        @NotBlank(message = "Confirme a senha.")
        private String confirmarSenha;

        @Size(max = 25, message = "Telefone deve ter no máximo 25 caracteres.")
        private String telefone;

        @NotBlank(message = "Informe o CPF.")
        @Pattern(regexp = "^(\\d{3}\\.?){3}-?\\d{2}$", message = "CPF inválido. Ex: 123.456.789-09")
        private String cpf;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataDeNascimento;

        // Getters/Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
        public String getConfirmarSenha() { return confirmarSenha; }
        public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
        public LocalDate getDataDeNascimento() { return dataDeNascimento; }
        public void setDataDeNascimento(LocalDate dataDeNascimento) { this.dataDeNascimento = dataDeNascimento; }
    }
}
