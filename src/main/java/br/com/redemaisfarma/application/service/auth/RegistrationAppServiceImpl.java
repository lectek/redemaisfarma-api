package br.com.redemaisfarma.application.service.auth;

import br.com.redemaisfarma.application.core.exception.CpfDuplicadoException;
import br.com.redemaisfarma.application.core.exception.EmailDuplicadoException;
import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;
import br.com.redemaisfarma.application.port.inbound.RegistrationAppService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationAppServiceImpl implements RegistrationAppService {

    private final UsuarioJpaRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public RegistrationAppServiceImpl(UsuarioJpaRepository usuarioRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void cadastrarNovoCliente(CadastroClienteRequestDTO request) {
        final String email = safe(request.getEmail());
        final String cpf   = normalizeCpf(request.getCpf());
        final String nome  = safe(request.getNome());
        final String raw   = safe(request.getSenha());

        // --- unicidade ---
        if (usuarioRepo.existsByEmail(email)) {
            throw new EmailDuplicadoException();
        }
        // usa a query flexível existente para checar CPF
        if (usuarioRepo.findByEmailOrCpf(cpf).isPresent()) {
            throw new CpfDuplicadoException();
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
