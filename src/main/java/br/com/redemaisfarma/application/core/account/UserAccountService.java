/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.core.account;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        UsuarioEntity u = (UsuarioEntity)this.usuarios.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usu\u00e1rio n\u00e3o encontrado."));
        if (!this.encoder.matches((CharSequence)senhaAtual, u.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        if (this.encoder.matches((CharSequence)novaSenha, u.getSenha())) {
            throw new IllegalArgumentException("A nova senha n\u00e3o pode ser igual \u00e0 senha atual.");
        }
        u.setSenha(this.encoder.encode((CharSequence)novaSenha));
        this.usuarios.save(u);
    }

    @Transactional
    public UsuarioEntity register(String nome, String email, String cpf, String senhaPura) {
        String cpfNorm;
        String emailNorm = email == null ? null : email.trim().toLowerCase();
        String string = cpfNorm = cpf == null ? null : cpf.replaceAll("[^0-9]", "");
        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("E-mail \u00e9 obrigat\u00f3rio.");
        }
        if (cpfNorm == null || cpfNorm.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 d\u00edgitos.");
        }
        if (this.usuarios.existsByEmailIgnoreCase(emailNorm)) {
            throw new IllegalArgumentException("E-mail j\u00e1 cadastrado.");
        }
        if (this.usuarios.existsByCpf(cpfNorm)) {
            throw new IllegalArgumentException("CPF j\u00e1 cadastrado.");
        }
        UsuarioEntity u = new UsuarioEntity();
        u.setNome(nome);
        u.setEmail(emailNorm);
        u.setCpf(cpfNorm);
        u.setSenha(this.encoder.encode((CharSequence)senhaPura));
        u.setRoles(Set.of(Role.of("ROLE_USER")));
        return (UsuarioEntity)this.usuarios.save(u);
    }
}

