package br.com.redemaisfarma.adapters.outbound.persistence.auth;

import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.domain.user.Usuario;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile({"dev","prod","default"}) // ativa em todos exceto "test" se quiser
public class AuthRepositoryJdbc implements AuthRepositoryPort {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    // contador simples em memória
    private final Map<String, Integer> failCount = new ConcurrentHashMap<>();

    public AuthRepositoryJdbc(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    // -------------------------------
    // Queries base
    // -------------------------------
    private static final String SQL_FIND_USER =
        """
        SELECT u.id, u.nome, u.email, u.cpf, u.senha
          FROM usuario u
         WHERE LOWER(u.email) = LOWER(?) OR u.cpf = ?
         LIMIT 1
        """;

    // Agora lemos a role como string
    private static final String SQL_FIND_ROLES =
        "SELECT r.role FROM usuario_roles r WHERE r.usuario_id = ?";

    private static final String SQL_EXISTS_EMAIL =
        "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?)";

    // Coluna correta conforme schema: ultimo_acesso
    private static final String SQL_UPDATE_LAST_ACCESS =
        "UPDATE usuario SET ultimo_acesso = ? WHERE id = ?";

    private static final String SQL_INSERT_USER =
        "INSERT INTO usuario (nome, email, cpf, senha) VALUES (?,?,?,?)";

    private static final String SQL_UPDATE_USER =
        "UPDATE usuario SET nome = ?, email = ?, cpf = ?, senha = ? WHERE id = ?";

    // -------------------------------
    // Mappers
    // -------------------------------
    private static final RowMapper<Usuario> USER_MAPPER = new RowMapper<>() {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Usuario(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("cpf"),
                rs.getString("senha"),
                new HashSet<>(), // Set<String> de roles (preenchemos depois)
                false
            );
        }
    };

    // -------------------------------
    // Helpers
    // -------------------------------
    private static String onlyDigits(String s) {
        if (s == null) return null;
        return s.replaceAll("\\D+", "");
    }

    private Usuario loadUserWithRolesByIdentifier(String identifier) {
        String cpfDigits = onlyDigits(identifier);

        List<Usuario> users = jdbc.query(SQL_FIND_USER, ps -> {
            ps.setString(1, identifier);
            ps.setString(2, cpfDigits);
        }, USER_MAPPER);

        if (users.isEmpty()) return null;

        Usuario u = users.get(0);

        // Busca as roles como String e coloca em Set<String>
        List<String> roles = jdbc.query(SQL_FIND_ROLES,
            ps -> ps.setLong(1, u.getId()),
            (rs, i) -> rs.getString(1));

        u.setRoles(new HashSet<>(roles));

        // VIP: se tem ROLE_VIP ou CPF especial
        boolean vip = roles.stream().anyMatch(r -> "ROLE_VIP".equalsIgnoreCase(r))
                        || "11122233344".equals(cpfDigits);
        u.setClienteVip(vip);

        return u;
    }

    // -------------------------------
    // Implementação da Port
    // -------------------------------
    @Override
    public Optional<Usuario> findByIdentifier(String identifier) {
        Usuario u = loadUserWithRolesByIdentifier(identifier);
        return Optional.ofNullable(u);
    }

    @Override
    public boolean isBlocked(String identifier) {
        return failCount.getOrDefault(identifier, 0) >= 5;
    }

    @Override
    public void registerFailedAttempt(String identifier) {
        failCount.merge(identifier, 1, Integer::sum);
    }

    @Override
    public void resetFailedAttempts(String identifier) {
        failCount.remove(identifier);
    }

    @Override
    public boolean updateLastAccess(Long userId) {
        int updated = jdbc.update(SQL_UPDATE_LAST_ACCESS, LocalDateTime.now(), userId);
        return updated > 0;
    }

    @Override
    public void save(Usuario user) {
        if (user.getId() == null) {
            jdbc.update(SQL_INSERT_USER,
                user.getNome(),
                user.getEmail(),
                user.getCpf(),
                user.getSenha());
        } else {
            jdbc.update(SQL_UPDATE_USER,
                user.getNome(),
                user.getEmail(),
                user.getCpf(),
                user.getSenha(),
                user.getId());
        }
        // Persistir roles em usuario_roles exigiria upsert separado (se precisar, faço já).
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer c = jdbc.query(SQL_EXISTS_EMAIL, ps -> ps.setString(1, email),
            rs -> rs.next() ? rs.getInt(1) : 0);
        return c != null && c > 0;
    }

    @Override
    public Usuario authenticate(String identifier, String password) {
        Usuario u = loadUserWithRolesByIdentifier(identifier);
        if (u == null) throw new EmptyResultDataAccessException(1);

        if (!encoder.matches(password, u.getSenha())) {
            throw new EmptyResultDataAccessException(1);
        }
        return u;
    }

    @Override
    public boolean isClienteVip(String identifier) {
        return findByIdentifier(identifier).map(Usuario::isClienteVip).orElse(false);
    }
}
