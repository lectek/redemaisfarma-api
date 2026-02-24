package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.beans.PropertyEditor;
import java.time.LocalDate;

import br.com.redemaisfarma.application.core.exception.CpfDuplicadoException;
import br.com.redemaisfarma.application.core.exception.EmailDuplicadoException;
import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import br.com.redemaisfarma.application.port.inbound.RegistrationAppService;

@Controller
@RequestMapping("/auth")
public class AuthRegistrationController {

    private final RegistrationAppService registrationAppService;
    private final OtpServicePort otpService;

    public AuthRegistrationController(RegistrationAppService registrationAppService, OtpServicePort otpService) {
        this.registrationAppService = registrationAppService;
        this.otpService = otpService;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, (PropertyEditor) new StringTrimmerEditor(true));
    }

    @GetMapping("/cliente/cadastro")
    public String cadastroForm(Model model) {
        if (!model.containsAttribute("cadastro")) {
            model.addAttribute("cadastro", new CadastroClienteForm());
        }
        // agora renderiza o template em src/main/resources/templates/pages/auth/cadastro-cliente.html
        return "pages/auth/cadastro-cliente";
    }

    @PostMapping("/cliente/cadastro")
    public String processar(
            @Valid @ModelAttribute("cadastro") CadastroClienteForm form,
            BindingResult br,
            Model model
    ) {
        // Honeypot anti-bot
        if (hasText(form.getWebsite())) {
            br.reject("bot", "Operação não permitida.");
            return "pages/auth/cadastro-cliente";
        }

        // Normalizações
        form.setCpf(normalizeCpf(form.getCpf()));
        form.setTelefone(normalizePhone(form.getTelefone()));

        // Confirmação de senha
        if (hasText(form.getSenha()) && !form.getSenha().equals(form.getConfirmarSenha())) {
            br.rejectValue("confirmarSenha", "mismatch", "As senhas não conferem.");
        }

        if (br.hasErrors()) {
            return "pages/auth/cadastro-cliente";
        }

        if (!hasText(form.getOtpToken())) {
            br.reject("otp.obrigatorio", "Confirme o codigo de verificacao antes de criar a conta.");
            return "pages/auth/cadastro-cliente";
        }

        String otpDestino = resolveOtpDestino(form);
        if (!hasText(otpDestino) || !otpService.consumeTokenForDestino(form.getOtpToken(), otpDestino)) {
            br.reject("otp.invalido", "Codigo de verificacao invalido ou expirado.");
            return "pages/auth/cadastro-cliente";
        }

        try {
            registrationAppService.cadastrarNovoCliente(toRequest(form));
            return "redirect:/auth/login?from=cadastro_ok";
        } catch (EmailDuplicadoException e) {
            br.rejectValue("email", "duplicado", "E-mail já cadastrado.");
            return "pages/auth/cadastro-cliente";
        } catch (CpfDuplicadoException e) {
            br.rejectValue("cpf", "duplicado", "CPF já cadastrado.");
            return "pages/auth/cadastro-cliente";
        } catch (Exception e) {
            br.reject("erro", "Não foi possível concluir o cadastro agora.");
            return "pages/auth/cadastro-cliente";
        }
    }

    private static boolean hasText(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private static String normalizeCpf(String cpf) {
        if (!hasText(cpf)) return cpf;
        String onlyDigits = cpf.replaceAll("\\D", "");
        return onlyDigits.length() == 11 ? onlyDigits : cpf;
    }

    private static String normalizePhone(String phone) {
        if (!hasText(phone)) return phone;
        String cleaned = phone.replaceAll("[^+()\\d\\s-]", "");
        return cleaned.trim();
    }

    private static String resolveOtpDestino(CadastroClienteForm form) {
        if (form == null) return null;
        String canal = hasText(form.getCanalOtp()) ? form.getCanalOtp().trim().toLowerCase() : "email";
        if ("sms".equals(canal)) {
            return normalizePhone(form.getTelefone());
        }
        return form.getEmail();
    }

    // ==== DTO do formulário ====
    public static class CadastroClienteForm {

        @NotBlank(message = "Informe seu nome completo.")
        private String nome;

        @Email(message = "E-mail inválido.")
        @NotBlank(message = "Informe um e-mail válido.")
        private String email;

        @NotBlank(message = "Informe o CPF.")
        @Pattern(
            regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
            message = "CPF inválido. Use 11 dígitos (com ou sem máscara)."
        )
        private String cpf;

        @Pattern(
            regexp = "^$|^[+()\\d\\s-]{8,20}$",
            message = "Telefone inválido."
        )
        private String telefone;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataDeNascimento;

        @Size(min = 8, max = 128, message = "A senha deve ter entre 8 e 128 caracteres.")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,128}$",
            message = "A senha precisa de maiúscula, minúscula, número e caractere especial."
        )
        private String senha;

        @NotBlank(message = "Confirme sua senha.")
        private String confirmarSenha;

        // campos para OTP e honeypot
        private String canalOtp;
        private String otpDeliveryId;
        private String otpToken;
        /** Campo escondido (honeypot) — deve ficar vazio */
        private String website;

        // getters/setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }

        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }

        public LocalDate getDataDeNascimento() { return dataDeNascimento; }
        public void setDataDeNascimento(LocalDate dataDeNascimento) { this.dataDeNascimento = dataDeNascimento; }

        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }

        public String getConfirmarSenha() { return confirmarSenha; }
        public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }

        public String getCanalOtp() { return canalOtp; }
        public void setCanalOtp(String canalOtp) { this.canalOtp = canalOtp; }

        public String getOtpDeliveryId() { return otpDeliveryId; }
        public void setOtpDeliveryId(String otpDeliveryId) { this.otpDeliveryId = otpDeliveryId; }

        public String getOtpToken() { return otpToken; }
        public void setOtpToken(String otpToken) { this.otpToken = otpToken; }

        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
    }

    // Exceções de domínio (mantidas para compatibilidade)
    private static CadastroClienteRequestDTO toRequest(CadastroClienteForm form) {
        CadastroClienteRequestDTO dto = new CadastroClienteRequestDTO();
        dto.setNome(form.getNome());
        dto.setEmail(form.getEmail());
        dto.setCpf(form.getCpf());
        dto.setTelefone(form.getTelefone());
        dto.setDataDeNascimento(form.getDataDeNascimento());
        dto.setSenha(form.getSenha());
        dto.setConfirmarSenha(form.getConfirmarSenha());
        dto.setCanalOtp(form.getCanalOtp());
        dto.setOtpDeliveryId(form.getOtpDeliveryId());
        dto.setOtpToken(form.getOtpToken());
        dto.setWebsite(form.getWebsite());
        return dto;
    }

    // Porta de aplicação — interface para o caso real
}
