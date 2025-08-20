package br.com.redemaisfarma.adapters.outbound.auth;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.mapper.UsuarioMapper;
import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.core.exception.UserBlockedException;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.domain.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AuthRepositoryImpl implements AuthRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthRepositoryImpl(UsuarioJpaRepository usuarioJpaRepository, UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByIdentifier(String identifier) {
        String norm = normalize(identifier);
        return usuarioJpaRepository.findByEmailOrCpf(norm).map(usuarioMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(String identifier) {
        String norm = normalize(identifier);
        Boolean blocked = usuarioJpaRepository.isBlockedByEmailOrCpf(norm);
        return blocked != null && blocked;
    }

    @Override
    @Transactional
    public void registerFailedAttempt(String identifier) {
        usuarioJpaRepository.registrarTentativaFalha(normalize(identifier));
    }

    @Override
    @Transactional
    public void resetFailedAttempts(String identifier) {
        usuarioJpaRepository.resetarTentativasFalhas(normalize(identifier));
    }

    @Override
    @Transactional
    public boolean updateLastAccess(Long userId) {
        return usuarioJpaRepository.updateUltimoAcesso(userId, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional
    public void save(Usuario user) {
        usuarioJpaRepository.save(usuarioMapper.toEntity(user));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioJpaRepository.existsByEmail(normalize(email));
    }

    @Override
    @Transactional
    public Usuario authenticate(String identifier, String rawPassword) {
        String norm = normalize(identifier);

        var usuarioEntity = usuarioJpaRepository.findByEmailOrCpf(norm).orElseThrow(InvalidCredentialsException::new);

        if (Boolean.TRUE.equals(usuarioJpaRepository.isBlockedByEmailOrCpf(norm))) {
            // precisa herdar de RuntimeException (veja classe abaixo)
            throw new UserBlockedException();
        }

        if (!passwordEncoder.matches(rawPassword, usuarioEntity.getSenha())) {
            usuarioJpaRepository.registrarTentativaFalha(norm);
            throw new InvalidCredentialsException();
        }

        usuarioJpaRepository.resetarTentativasFalhas(norm);
        usuarioJpaRepository.updateUltimoAcesso(usuarioEntity.getId(), LocalDateTime.now());

        return usuarioMapper.toDomain(usuarioEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClienteVip(String identifier) {
        // repositório retorna Optional<Boolean>
        return usuarioJpaRepository.verificarClienteVip(normalize(identifier)).orElse(Boolean.FALSE);
    }

    private String normalize(String identifier) {
        if (identifier == null)
            return null;
        String id = identifier.trim();
        return id.contains("@") ? id.toLowerCase() : id.replaceAll("\\D", "");
    }
}
