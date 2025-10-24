/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsuarioRepository
extends JpaRepository<UsuarioEntity, Long> {
    @Transactional(readOnly=true)
    public Optional<UsuarioEntity> findByEmailIgnoreCase(String var1);

    @Transactional(readOnly=true)
    public boolean existsByEmailIgnoreCase(String var1);

    @Transactional(readOnly=true)
    public Optional<UsuarioEntity> findByCpf(String var1);

    @Transactional(readOnly=true)
    public boolean existsByCpf(String var1);

    @Transactional(readOnly=true)
    @Query(value="select u\n  from UsuarioEntity u\n where lower(u.email) = lower(:ident)\n    or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')\n     = replace(replace(replace(:ident,'.',''),'-',''),' ','')\n")
    public Optional<UsuarioEntity> findByEmailOrCpf(@Param(value="ident") String var1);

    @Transactional(readOnly=true)
    @Query(value="select case when count(u) > 0 then true else false end\n  from UsuarioEntity u\n where lower(u.email) = lower(:ident)\n    or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')\n     = replace(replace(replace(:ident,'.',''),'-',''),' ','')\n")
    public boolean existsByEmailOrCpf(@Param(value="ident") String var1);
}

