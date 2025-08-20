package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.RefreshTokenEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.RefreshTokenJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
public class RefreshTokenJpaStore implements RefreshTokenStore {

    private final RefreshTokenJpaRepository repo;

    public RefreshTokenJpaStore(RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public RefreshToken save(RefreshToken rt) {
        RefreshTokenEntity entity = RefreshTokenMapper.toEntity(rt);
        RefreshTokenEntity saved = repo.save(entity);
        return RefreshTokenMapper.toModel(saved);
    }

    /**
     * Busca um refresh token pelo valor e garante que esteja válido (não revogado e não expirado no momento da
     * checagem).
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidByToken(String token) {
        Instant now = Instant.now();
        return repo.findByToken(token).map(RefreshTokenMapper::toModel)
                .filter(rt -> !rt.isRevoked() && !rt.isExpired(now));
    }

    /**
     * Marca um token específico como revogado (soft-revoke).
     */
    @Override
    public void revokeByToken(String token, Instant revokedAt) {
        repo.findByToken(token).ifPresent(e -> {
            e.setRevokedAt(revokedAt != null ? revokedAt : Instant.now());
            repo.save(e);
        });
    }

    /**
     * Revoga todos os tokens de um usuário; se tenantId for informado, restringe ao tenant correspondente. Implementado
     * apenas com métodos genéricos do JpaRepository para evitar dependência em queries customizadas.
     */
    @Override
    public void revokeAllForUser(Long userId, String tenantId, Instant revokedAt) {
        Instant when = revokedAt != null ? revokedAt : Instant.now();

        // Carrega todos e filtra em memória (funciona já com JpaRepository puro)
        List<RefreshTokenEntity> all = repo.findAll();
        List<RefreshTokenEntity> toRevoke = all.stream()
                .filter(e -> e.getUserId() != null && e.getUserId().equals(userId))
                .filter(e -> tenantId == null || tenantId.equals(e.getTenantId())).filter(e -> e.getRevokedAt() == null) // ainda
                                                                                                                         // não
                                                                                                                         // revogados
                .peek(e -> e.setRevokedAt(when)).collect(Collectors.toList());

        if (!toRevoke.isEmpty()) {
            repo.saveAll(toRevoke);
        }
    }

    /**
     * Gira o refresh token: revoga o antigo e salva o novo.
     */
    @Override
    public RefreshToken rotate(String oldToken, RefreshToken newToken, Instant revokedAt) {
        revokeByToken(oldToken, revokedAt);
        return save(newToken);
    }

    /**
     * Remove fisicamente tokens expirados (hard-delete). Implementado sem query customizada: carrega todos, filtra e
     * deleta. Retorna a quantidade removida.
     */
    @Override
    public long deleteExpired(Instant now) {
        Instant ref = (now != null ? now : Instant.now());

        List<RefreshTokenEntity> all = repo.findAll();
        List<RefreshTokenEntity> expired = all.stream()
                .filter(e -> e.getExpiresAt() != null && e.getExpiresAt().isBefore(ref)).collect(Collectors.toList());

        if (!expired.isEmpty()) {
            repo.deleteAll(expired);
        }
        return expired.size();
    }
}
