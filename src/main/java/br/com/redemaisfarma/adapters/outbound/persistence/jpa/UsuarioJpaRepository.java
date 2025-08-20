package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    // Busca por e-mail (case-insensitive) ou CPF já normalizado
    @Query("""
            SELECT u
              FROM UsuarioEntity u
             WHERE lower(u.email) = lower(:identifier) OR u.cpf = :identifier
            """)
    Optional<UsuarioEntity> findByEmailOrCpf(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<UsuarioEntity> findByClienteVipTrue();

    // ++ tentativas (trata null com coalesce)
    @Modifying
    @Transactional
    @Query("""
            UPDATE UsuarioEntity u
               SET u.tentativasFalhas = coalesce(u.tentativasFalhas, 0) + 1
             WHERE lower(u.email) = lower(:identifier) OR u.cpf = :identifier
            """)
    void registrarTentativaFalha(@Param("identifier") String identifier);

    // reset tentativas
    @Modifying
    @Transactional
    @Query("""
            UPDATE UsuarioEntity u
               SET u.tentativasFalhas = 0
             WHERE lower(u.email) = lower(:identifier) OR u.cpf = :identifier
            """)
    void resetarTentativasFalhas(@Param("identifier") String identifier);

    // bloqueio por tentativas (retorna wrapper para evitar NPE)
    @Query("""
            SELECT CASE WHEN coalesce(u.tentativasFalhas, 0) >= 5 THEN true ELSE false END
              FROM UsuarioEntity u
             WHERE lower(u.email) = lower(:identifier) OR u.cpf = :identifier
            """)
    Boolean isBlockedByEmailOrCpf(@Param("identifier") String identifier);

    // VIP (Optional para ausência)
    @Query("""
            SELECT u.clienteVip
              FROM UsuarioEntity u
             WHERE lower(u.email) = lower(:identifier) OR u.cpf = :identifier
            """)
    Optional<Boolean> verificarClienteVip(@Param("identifier") String identifier);

    // último acesso
    @Modifying
    @Transactional
    @Query("""
            UPDATE UsuarioEntity u
               SET u.ultimoAcesso = :data
             WHERE u.id = :userId
            """)
    int updateUltimoAcesso(@Param("userId") Long userId, @Param("data") LocalDateTime data);
}
