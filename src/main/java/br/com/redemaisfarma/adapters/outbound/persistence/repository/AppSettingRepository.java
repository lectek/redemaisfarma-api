/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppSettingRepository
extends JpaRepository<AppSettingEntity, Long> {
    public Optional<AppSettingEntity> findBySettingKey(String var1);

    public boolean existsBySettingKey(String var1);

    @Query(value="select s\nfrom AppSettingEntity s\nwhere (:q is null or :q = ''\n       or lower(s.settingKey) like lower(concat('%', :q, '%'))\n       or lower(s.description) like lower(concat('%', :q, '%')))\norder by s.settingKey asc\n")
    public Page<AppSettingEntity> search(String var1, Pageable var2);
}

