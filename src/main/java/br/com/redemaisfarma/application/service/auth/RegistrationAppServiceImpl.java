package br.com.redemaisfarma.application.service.auth;

import br.com.redemaisfarma.adapters.inbound.web.controller.auth.AuthRegistrationController;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationAppServiceImpl implements AuthRegistrationController.RegistrationAppService {

    private final UsuarioJpaRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public RegistrationAppServiceImpl(UsuarioJpaRepository usuarioRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void cadastrarNovoCliente(AuthRegistrationController.CadastroClienteForm form) {
        final String email = safe(form.getEmail());
        final String cpf   = normalizeCpf(form.getCpf());
        final String nome  = safe(form.getNome());
        final String raw   = safe(form.getSenha());

        // --- unicidade ---
        if (usuarioRepo.existsByEmail(email)) {
            throw new AuthRegistrationController.EmailDuplicadoException();
        }
        // usa a query flexível existente para checar CPF
        if (usuarioRepo.findByEmailOrCpf(cpf).isPresent()) {
            throw new AuthRegistrationController.CpfDuplicadoException();
        }

        // --- montar entidade ---
        UsuarioEntity u = new UsuarioEntity();
        u.setEmail(email);
        u.setCpf(cpf);
        u.setNome(nome);

        // senha obrigatória (sua entidade tem @NotBlank)
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Senha obrigatória.");
        }
        u.setSenha(encoder.encode(raw));

        // role padrão
        u.addRole(Role.of("ROLE_CLIENTE"));

        usuarioRepo.save(u);
    }

    // helpers
    private static String safe(String v) { return v == null ? null : v.trim(); }

    private static String normalizeCpf(String value) {
        if (value == null) return null;
        String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
