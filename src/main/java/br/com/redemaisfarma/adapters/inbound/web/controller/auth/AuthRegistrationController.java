/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.Email$List
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotBlank$List
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Pattern$List
 *  jakarta.validation.constraints.Size
 *  jakarta.validation.constraints.Size$List
 *  org.springframework.beans.propertyeditors.StringTrimmerEditor
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.format.annotation.DateTimeFormat$ISO
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.WebDataBinder
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.InitBinder
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.beans.PropertyEditor;
import java.time.LocalDate;
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

@Controller
@RequestMapping(value={"/auth"})
public class AuthRegistrationController {
    private final RegistrationAppService registrationAppService;

    public AuthRegistrationController(RegistrationAppService registrationAppService) {
        this.registrationAppService = registrationAppService;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, (PropertyEditor)new StringTrimmerEditor(true));
    }

    @GetMapping(value={"/cadastro-cliente"})
    public String cadastroForm(Model model) {
        if (!model.containsAttribute("cadastro")) {
            model.addAttribute("cadastro", (Object)new CadastroClienteForm());
        }
        return "pages/auth/cadastro-cliente";
    }

    @PostMapping(value={"/cadastro-cliente"})
    public String processar(@Valid @ModelAttribute(value="cadastro") CadastroClienteForm form, BindingResult br, Model model) {
        if (AuthRegistrationController.hasText(form.getWebsite())) {
            br.reject("bot", "Opera\u00e7\u00e3o n\u00e3o permitida.");
            return "pages/auth/cadastro-cliente";
        }
        form.setCpf(AuthRegistrationController.normalizeCpf(form.getCpf()));
        form.setTelefone(AuthRegistrationController.normalizePhone(form.getTelefone()));
        if (AuthRegistrationController.hasText(form.getSenha()) && !form.getSenha().equals(form.getConfirmarSenha())) {
            br.rejectValue("confirmarSenha", "mismatch", "As senhas n\u00e3o conferem.");
        }
        if (br.hasErrors()) {
            return "pages/auth/cadastro-cliente";
        }
        try {
            this.registrationAppService.cadastrarNovoCliente(form);
            return "redirect:/auth/login?from=cadastro_ok";
        }
        catch (EmailDuplicadoException e) {
            br.rejectValue("email", "duplicado", "E-mail j\u00e1 cadastrado.");
            return "pages/auth/cadastro-cliente";
        }
        catch (CpfDuplicadoException e) {
            br.rejectValue("cpf", "duplicado", "CPF j\u00e1 cadastrado.");
            return "pages/auth/cadastro-cliente";
        }
        catch (Exception e) {
            br.reject("erro", "N\u00e3o foi poss\u00edvel concluir o cadastro agora.");
            return "pages/auth/cadastro-cliente";
        }
    }

    private static boolean hasText(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private static String normalizeCpf(String cpf) {
        if (!AuthRegistrationController.hasText(cpf)) {
            return cpf;
        }
        String onlyDigits = cpf.replaceAll("\\D", "");
        return onlyDigits.length() == 11 ? onlyDigits : cpf;
    }

    private static String normalizePhone(String phone) {
        if (!AuthRegistrationController.hasText(phone)) {
            return phone;
        }
        String cleaned = phone.replaceAll("[^+()\\d\\s-]", "");
        return cleaned.trim();
    }

    public static class CadastroClienteForm {
        @NotBlank(message="Informe seu nome completo.")
@NotBlank(message="Informe seu nome completo.")
        private @NotBlank(message="Informe seu nome completo.")
@NotBlank(message="Informe seu nome completo.") String nome;
        @Email(message="E-mail inv\u00e1lido.")
@Email(message="E-mail inv\u00e1lido.")
        @NotBlank(message="Informe um e-mail v\u00e1lido.")
@NotBlank(message="Informe um e-mail v\u00e1lido.")
        private @Email(message="E-mail inv\u00e1lido.")
@Email(message="E-mail inv\u00e1lido.") @NotBlank(message="Informe um e-mail v\u00e1lido.")
@NotBlank(message="Informe um e-mail v\u00e1lido.") String email;
        @NotBlank(message="Informe o CPF.")
@NotBlank(message="Informe o CPF.")
        @Pattern(regexp="^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11$", message="CPF inv\u00e1lido. Use 11 d\u00edgitos (com ou sem m\u00e1scara)."), @Pattern(regexp="^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$", message="CPF inv\u00e1lido. Use 11 d\u00edgitos (com ou sem m\u00e1scara).")})
        private @NotBlank(message="Informe o CPF.")
@NotBlank(message="Informe o CPF.") @Pattern(regexp="^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11$", message="CPF inv\u00e1lido. Use 11 d\u00edgitos (com ou sem m\u00e1scara)."), @Pattern(regexp="^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$", message="CPF inv\u00e1lido. Use 11 d\u00edgitos (com ou sem m\u00e1scara).")}) String cpf;
        @Pattern(regexp="^$|^[+()\\d\\s-]{8,20}$", message="Telefone inv\u00e1lido.")
@Pattern(regexp="^$|^[+()\\d\\s-]{8,20}$", message="Telefone inv\u00e1lido.")
        private @Pattern(regexp="^$|^[+()\\d\\s-]{8,20}$", message="Telefone inv\u00e1lido.")
@Pattern(regexp="^$|^[+()\\d\\s-]{8,20}$", message="Telefone inv\u00e1lido.") String telefone;
        @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
        private LocalDate dataDeNascimento;
        @Size(min=8, max=128, message="A senha deve ter entre 8 e 128 caracteres.")
@Size(min=8, max=128, message="A senha deve ter entre 8 e 128 caracteres.")
        @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}$", message="A senha precisa de mai\u00fascula, min\u00fascula, n\u00famero e s\u00edmbolo.")
@Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}$", message="A senha precisa de mai\u00fascula, min\u00fascula, n\u00famero e s\u00edmbolo.")
        private @Size(min=8, max=128, message="A senha deve ter entre 8 e 128 caracteres.")
@Size(min=8, max=128, message="A senha deve ter entre 8 e 128 caracteres.") @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}$", message="A senha precisa de mai\u00fascula, min\u00fascula, n\u00famero e s\u00edmbolo.")
@Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}$", message="A senha precisa de mai\u00fascula, min\u00fascula, n\u00famero e s\u00edmbolo.") String senha;
        @NotBlank(message="Confirme sua senha.")
@NotBlank(message="Confirme sua senha.")
        private @NotBlank(message="Confirme sua senha.")
@NotBlank(message="Confirme sua senha.") String confirmarSenha;
        private String canalOtp;
        private String otpDeliveryId;
        private String otpToken;
        private String website;

        public String getNome() {
            return this.nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCpf() {
            return this.cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public String getTelefone() {
            return this.telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public LocalDate getDataDeNascimento() {
            return this.dataDeNascimento;
        }

        public void setDataDeNascimento(LocalDate dataDeNascimento) {
            this.dataDeNascimento = dataDeNascimento;
        }

        public String getSenha() {
            return this.senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }

        public String getConfirmarSenha() {
            return this.confirmarSenha;
        }

        public void setConfirmarSenha(String confirmarSenha) {
            this.confirmarSenha = confirmarSenha;
        }

        public String getCanalOtp() {
            return this.canalOtp;
        }

        public void setCanalOtp(String canalOtp) {
            this.canalOtp = canalOtp;
        }

        public String getOtpDeliveryId() {
            return this.otpDeliveryId;
        }

        public void setOtpDeliveryId(String otpDeliveryId) {
            this.otpDeliveryId = otpDeliveryId;
        }

        public String getOtpToken() {
            return this.otpToken;
        }

        public void setOtpToken(String otpToken) {
            this.otpToken = otpToken;
        }

        public String getWebsite() {
            return this.website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }
    }

    public static class CpfDuplicadoException
    extends RuntimeException {
    }

    public static class EmailDuplicadoException
    extends RuntimeException {
    }

    public static interface RegistrationAppService {
        public void cadastrarNovoCliente(CadastroClienteForm var1);
    }
}

