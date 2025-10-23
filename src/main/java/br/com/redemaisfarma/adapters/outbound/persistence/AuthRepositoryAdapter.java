package br.com.redemaisfarma.adapters.outbound.persistence;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.domain.user.Role;
import br.com.redemaisfarma.domain.user.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class AuthRepositoryAdapter implements AuthRepositoryPort {

    private final int MAX_TENTATIVAS;
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public AuthRepositoryAdapter(
            UsuarioRepository repo,
            PasswordEncoder encoder,
            @Value("${auth.max-tentativas:5}") int maxTentativas
    ) {
        this.repo = repo;
        this.encoder = encoder;
        this.MAX_TENTATIVAS = maxTentativas;
    }

    // ========== QUERIES ==========

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByIdentifier(String identifier) {
        return findEntityByIdentifier(identifier).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(String identifier) {
        return findEntityByIdentifier(identifier)
                .map(u -> u.getTentativasFalhas() != null && u.getTentativasFalhas() >= MAX_TENTATIVAS)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repo.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClienteVip(String identifier) {
        return findEntityByIdentifier(identifier)
                .map(u -> Boolean.TRUE.equals(u.getClienteVip()))
                .orElse(false);
    }

    // ========== COMANDOS ==========

    @Override
    @Transactional
    public void registerFailedAttempt(String identifier) {
        findEntityByIdentifier(identifier).ifPresent(u -> {
            int atual = u.getTentativasFalhas() == null ? 0 : u.getTentativasFalhas();
            u.setTentativasFalhas(atual + 1);
            repo.save(u);
        });
    }

    @Override
    @Transactional
    public void resetFailedAttempts(String identifier) {
        findEntityByIdentifier(identifier).ifPresent(u -> {
            u.setTentativasFalhas(0);
            repo.save(u);
        });
    }

    @Override
    @Transactional
    public boolean updateLastAccess(Long userId) {
        return repo.findById(userId).map(u -> {
            u.setUltimoAcesso(now());
            repo.save(u);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public void save(Usuario user) {
        UsuarioEntity e = toEntity(user);

        // normalização
        e.setEmail(normalizeEmail(e.getEmail()));
        e.setCpf(normalizeCpf(e.getCpf()));

        // codifica senha se necessário
        if (!isEncoded(e.getSenha())) {
            e.setSenha(encoder.encode(e.getSenha()));
        }

        // defaults
        if (e.getRoles() == null || e.getRoles().isEmpty()) {
            // Default para ROLE_USER (assumindo que já existe na tabela 'roles')
            e.setRoles(toRoleEntities(Set.of("ROLE_USER")));
        }
        if (e.getTentativasFalhas() == null) {
            e.setTentativasFalhas(0);
        }
        if (e.getClienteVip() == null) {
            e.setClienteVip(Boolean.FALSE);
        }

        repo.save(e);
    }

    @Override
    @Transactional
    public Usuario authenticate(String identifier, String password) {
        UsuarioEntity e = findEntityByIdentifier(identifier)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        // bloqueado?
        if (e.getTentativasFalhas() != null && e.getTentativasFalhas() >= MAX_TENTATIVAS) {
            throw new IllegalStateException("Usuário bloqueado por tentativas inválidas");
        }

        if (!encoder.matches(password, e.getSenha())) {
            int novo = (e.getTentativasFalhas() == null ? 0 : e.getTentativasFalhas()) + 1;
            e.setTentativasFalhas(novo);
            repo.save(e);
            throw new IllegalArgumentException("Senha inválida");
        }

        // sucesso: zera tentativas e atualiza ultimo_acesso
        e.setTentativasFalhas(0);
        e.setUltimoAcesso(now());
        repo.save(e);

        return toDomain(e);
    }

    // ========== HELPERS ==========

    @Transactional(readOnly = true)
    protected Optional<UsuarioEntity> findEntityByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) return Optional.empty();

        String ident = identifier.trim();

        // e-mail? (aceita caso errado)
        if (ident.contains("@")) {
            return repo.findByEmailIgnoreCase(normalizeEmail(ident));
        }

        // CPF de 11 dígitos?
        String digits = normalizeCpf(ident);
        if (digits.length() == 11) {
            return repo.findByCpf(digits);
        }

        // fallback: tenta consulta combinada (e-mail case-insensitive ou CPF com máscara)
        return repo.findByEmailOrCpf(ident);
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private boolean isEncoded(String rawOrEncoded) {
        if (rawOrEncoded == null) return false;
        // heurística comum para BCrypt
        return rawOrEncoded.startsWith("$2a$")
                || rawOrEncoded.startsWith("$2b$")
                || rawOrEncoded.startsWith("$2y$");
    }

    // ---- Converte entidade -> domínio (Set<Role> -> Set<String>)
    private Usuario toDomain(UsuarioEntity e) {
        Set<String> roleNames = toRoleNames(e.getRoles());
        return new Usuario(
                e.getId(),
                e.getNome(),
                e.getEmail(),
                e.getCpf(),
                e.getSenha(),
                roleNames,
                Boolean.TRUE.equals(e.getClienteVip())
        );
    }

    // ---- Converte domínio -> entidade (Set<String> -> Set<Role>)
    private UsuarioEntity toEntity(Usuario d) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setEmail(d.getEmail());
        e.setCpf(d.getCpf());
        e.setSenha(d.getSenha());
        e.setClienteVip(d.isClienteVip());
        e.setRoles(toRoleEntities(d.getRoles()));
        return e;
    }

    // ---- Utils de conversão de papéis
    private Set<String> toRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::getNome)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Role> toRoleEntities(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) return new HashSet<>();
        return roleNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(Role::of) // fábrica simples; assume que os papéis já existem na tabela `roles`
                .collect(Collectors.toCollection(HashSet::new));
    }
}
