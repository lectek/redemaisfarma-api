package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClienteNotificacaoRepository extends JpaRepository<ClienteNotificacaoEntity, Long> {

    @Query("""
            select n
              from ClienteNotificacaoEntity n
             where n.usuario.id = :usuarioId
             order by n.createdAt desc
            """)
    List<ClienteNotificacaoEntity> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);

    @Transactional
    @Modifying
    @Query("""
            update ClienteNotificacaoEntity n
               set n.lida = true
             where n.usuario.id = :usuarioId
               and n.id in :ids
            """)
    int markAsRead(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}
