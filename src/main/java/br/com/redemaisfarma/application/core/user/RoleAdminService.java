// src/main/java/br/com/redemaisfarma/application/core/user/RoleAdminService.java
package br.com.redemaisfarma.application.core.user;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import br.com.redemaisfarma.user.audit.RoleAudit;
import br.com.redemaisfarma.user.audit.RoleAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RoleAdminService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UsuarioRepository usuarios;
    private final RoleAuditRepository auditRepo;

    public RoleAdminService(UsuarioRepository usuarios, RoleAuditRepository auditRepo) {
        this.usuarios = usuarios;
        this.auditRepo = auditRepo;
    }

    @Transactional
    public void grantAdmin(Long userId, HttpServletRequest req) {
        var u = usuarios.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        boolean already = u.getRoles().stream()
                .anyMatch(r -> ROLE_ADMIN.equalsIgnoreCase(r.getName()));
        if (!already) {
            u.addRole(Role.of(ROLE_ADMIN));
            usuarios.save(u);
            audit("GRANT", userId, ROLE_ADMIN, req);
        }
    }

    @Transactional
    public void revokeAdmin(Long userId, HttpServletRequest req) {
        var u = usuarios.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // evita remover de si mesmo
        String current = currentUserEmail();
        if (current != null && current.equalsIgnoreCase(u.getEmail())) {
            throw new IllegalStateException("Você não pode remover o próprio ADMIN.");
        }

        boolean removed = u.getRoles().removeIf(r -> ROLE_ADMIN.equalsIgnoreCase(r.getName()));
        if (removed) {
            usuarios.save(u);
            audit("REVOKE", userId, ROLE_ADMIN, req);
        }
    }

    private void audit(String action, Long targetUserId, String roleName, HttpServletRequest req) {
        var a = new RoleAudit();
        a.setAction(action);
        a.setTargetUserId(targetUserId);
        a.setRoleName(roleName);
        a.setActorEmail(currentUserEmail());
        a.setIp(remoteIp(req));
        a.setUserAgent(req != null ? req.getHeader("User-Agent") : null);
        a.setCreatedAt(Instant.now());
        auditRepo.save(a);
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private String remoteIp(HttpServletRequest req) {
        if (req == null) return null;
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
