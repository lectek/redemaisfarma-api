package br.com.redemaisfarma.application.core.account;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserAccountService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;

    public UserAccountService(
            UsuarioRepository usuarios,
            ClienteRepository clientes,
            PasswordEncoder encoder
    ) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.encoder = encoder;
    }

    @Transactional
    public void changePassword(Long userId, String senhaAtual, String novaSenha) {
        UsuarioEntity u = usuarios.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!encoder.matches(senhaAtual, u.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        if (encoder.matches(novaSenha, u.getSenha())) {
            throw new IllegalArgumentException("A nova senha não pode ser igual à senha atual.");
        }
        u.setSenha(encoder.encode(novaSenha));
        usuarios.save(u);
    }

    @Transactional
    public UsuarioEntity register(String nome, String email, String cpf, String senhaPura) {
        String emailNorm = normalizeEmail(email);
        String nameNorm = deriveName(nome, emailNorm);
        String cpfNorm = normalizeCpf(cpf);

        if (usuarios.existsByEmailIgnoreCase(emailNorm)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        ensureUniqueCpf(cpfNorm);

        UsuarioEntity u = new UsuarioEntity();
        u.setNome(nameNorm);
        u.setEmail(emailNorm);
        u.setCpf(cpfNorm);
        u.setSenha(encoder.encode(senhaPura));
        u.setRoles(Set.of(Role.of("ROLE_USER")));
        UsuarioEntity salvo = usuarios.save(u);

        syncClienteCadastro(emailNorm, cpfNorm, nameNorm, salvo.getSenha());
        return salvo;
    }

    private void syncClienteCadastro(
            final String email,
            final String cpf,
            final String nome,
            final String senhaCodificada
    ) {
        final Optional<ClienteEntity> byEmail = clientes.findByEmailIgnoreCase(email);
        final Optional<ClienteEntity> byCpf = clientes.findByCpf(cpf);

        if (byEmail.isPresent() && byCpf.isPresent()
                && !byEmail.get().getId().equals(byCpf.get().getId())) {
            throw new IllegalStateException("Cadastro inconsistente para email/cpf.");
        }

        final ClienteEntity cliente = byEmail.or(() -> byCpf).orElseGet(ClienteEntity::new);
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        cliente.setAtivo(true);
        cliente.setSenha(senhaCodificada);
        clientes.save(cliente);
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }
        String trimmed = email.trim().toLowerCase();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }
        return trimmed;
    }

    private static String deriveName(String candidate, String email) {
        if (candidate != null && !candidate.isBlank()) {
            return candidate.trim();
        }
        int at = email.indexOf('@');
        if (at > 0) {
            return email.substring(0, at);
        }
        return "Cliente";
    }

    private String normalizeCpf(String input) {
        if (input == null || input.isBlank()) {
            return generateUniqueCpf();
        }
        String digits = input.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos.");
        }
        return digits;
    }

    private void ensureUniqueCpf(String cpfNorm) {
        if (cpfNorm == null) {
            throw new IllegalStateException("CPF não pode ser gerado.");
        }
        if (usuarios.existsByCpf(cpfNorm)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }
    }

    private String generateUniqueCpf() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = String.format("%011d", Math.abs(System.nanoTime()) % 100000000000L);
            if (!usuarios.existsByCpf(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Não foi possível gerar CPF temporário único.");
    }
}
