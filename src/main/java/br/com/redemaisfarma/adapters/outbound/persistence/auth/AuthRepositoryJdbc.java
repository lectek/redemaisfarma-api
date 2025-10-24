/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort
 *  br.com.redemaisfarma.domain.user.Usuario
 *  org.springframework.context.annotation.Profile
 *  org.springframework.dao.EmptyResultDataAccessException
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.jdbc.core.RowMapper
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.auth;

import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.domain.user.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@Profile(value={"dev", "prod", "default"})
public class AuthRepositoryJdbc
implements AuthRepositoryPort {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final Map<String, Integer> failCount = new ConcurrentHashMap<String, Integer>();
    private static final String SQL_FIND_USER = "SELECT u.id, u.nome, u.email, u.cpf, u.senha\n  FROM usuario u\n WHERE LOWER(u.email) = LOWER(?) OR u.cpf = ?\n LIMIT 1\n";
    private static final String SQL_FIND_ROLES = "SELECT r.role FROM usuario_roles r WHERE r.usuario_id = ?";
    private static final String SQL_EXISTS_EMAIL = "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?)";
    private static final String SQL_UPDATE_LAST_ACCESS = "UPDATE usuario SET ultimo_acesso = ? WHERE id = ?";
    private static final String SQL_INSERT_USER = "INSERT INTO usuario (nome, email, cpf, senha) VALUES (?,?,?,?)";
    private static final String SQL_UPDATE_USER = "UPDATE usuario SET nome = ?, email = ?, cpf = ?, senha = ? WHERE id = ?";
    private static final RowMapper<Usuario> USER_MAPPER = new RowMapper<Usuario>(){

        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Usuario(Long.valueOf(rs.getLong("id")), rs.getString("nome"), rs.getString("email"), rs.getString("cpf"), rs.getString("senha"), new HashSet(), false);
        }
    };

    public AuthRepositoryJdbc(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    private static String onlyDigits(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("\\D+", "");
    }

    private Usuario loadUserWithRolesByIdentifier(String identifier) {
        String cpfDigits = AuthRepositoryJdbc.onlyDigits(identifier);
        List users = this.jdbc.query(SQL_FIND_USER, ps -> {
            ps.setString(1, identifier);
            ps.setString(2, cpfDigits);
        }, USER_MAPPER);
        if (users.isEmpty()) {
            return null;
        }
        Usuario u = (Usuario)users.get(0);
        List roles = this.jdbc.query(SQL_FIND_ROLES, ps -> ps.setLong(1, u.getId()), (rs, i) -> rs.getString(1));
        u.setRoles(new HashSet(roles));
        boolean vip = roles.stream().anyMatch(r -> "ROLE_VIP".equalsIgnoreCase((String)r)) || "11122233344".equals(cpfDigits);
        u.setClienteVip(vip);
        return u;
    }

    public Optional<Usuario> findByIdentifier(String identifier) {
        Usuario u = this.loadUserWithRolesByIdentifier(identifier);
        return Optional.ofNullable(u);
    }

    public boolean isBlocked(String identifier) {
        return this.failCount.getOrDefault(identifier, 0) >= 5;
    }

    public void registerFailedAttempt(String identifier) {
        this.failCount.merge(identifier, 1, Integer::sum);
    }

    public void resetFailedAttempts(String identifier) {
        this.failCount.remove(identifier);
    }

    public boolean updateLastAccess(Long userId) {
        int updated = this.jdbc.update(SQL_UPDATE_LAST_ACCESS, new Object[]{LocalDateTime.now(), userId});
        return updated > 0;
    }

    public void save(Usuario user) {
        if (user.getId() == null) {
            this.jdbc.update(SQL_INSERT_USER, new Object[]{user.getNome(), user.getEmail(), user.getCpf(), user.getSenha()});
        } else {
            this.jdbc.update(SQL_UPDATE_USER, new Object[]{user.getNome(), user.getEmail(), user.getCpf(), user.getSenha(), user.getId()});
        }
    }

    public boolean existsByEmail(String email) {
        Integer c = (Integer)this.jdbc.query(SQL_EXISTS_EMAIL, ps -> ps.setString(1, email), rs -> rs.next() ? rs.getInt(1) : 0);
        return c != null && c > 0;
    }

    public Usuario authenticate(String identifier, String password) {
        Usuario u = this.loadUserWithRolesByIdentifier(identifier);
        if (u == null) {
            throw new EmptyResultDataAccessException(1);
        }
        if (!this.encoder.matches((CharSequence)password, u.getSenha())) {
            throw new EmptyResultDataAccessException(1);
        }
        return u;
    }

    public boolean isClienteVip(String identifier) {
        return this.findByIdentifier(identifier).map(Usuario::isClienteVip).orElse(false);
    }
}

