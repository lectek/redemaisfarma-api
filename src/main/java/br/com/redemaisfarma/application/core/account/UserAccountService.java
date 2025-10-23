// src/main/java/br/com/redemaisfarma/application/core/account/UserAccountService.java
package br.com.redemaisfarma.application.core.account;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserAccountService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UserAccountService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
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
        String emailNorm = email == null ? null : email.trim().toLowerCase();
        String cpfNorm   = cpf == null ? null : cpf.replaceAll("[^0-9]", "");

        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }
        if (cpfNorm == null || cpfNorm.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos.");
        }

        if (usuarios.existsByEmailIgnoreCase(emailNorm)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        if (usuarios.existsByCpf(cpfNorm)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        UsuarioEntity u = new UsuarioEntity();
        u.setNome(nome);
        u.setEmail(emailNorm);
        u.setCpf(cpfNorm);
        u.setSenha(encoder.encode(senhaPura));

        // role padrão como entidade
        u.setRoles(Set.of(Role.of("ROLE_USER")));

        return usuarios.save(u);
    }
}
