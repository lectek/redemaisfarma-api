package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @Transactional(readOnly = true)
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    @Transactional(readOnly = true)
    boolean existsByEmailIgnoreCase(String email);

    @Transactional(readOnly = true)
    Optional<UsuarioEntity> findByCpf(String cpf);

    @Transactional(readOnly = true)
    boolean existsByCpf(String cpf);

    @Transactional(readOnly = true)
    @Query("""
           select u
             from UsuarioEntity u
            where lower(u.email) = lower(:ident)
               or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
                = replace(replace(replace(:ident,'.',''),'-',''),' ','')
           """)
    Optional<UsuarioEntity> findByEmailOrCpf(@Param("ident") String ident);

    @Transactional(readOnly = true)
    @Query("""
           select case when count(u) > 0 then true else false end
             from UsuarioEntity u
            where lower(u.email) = lower(:ident)
               or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
                = replace(replace(replace(:ident,'.',''),'-',''),' ','')
           """)
    boolean existsByEmailOrCpf(@Param("ident") String ident);
}
