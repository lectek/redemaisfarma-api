/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailDeliveryRepository
extends JpaRepository<EmailDelivery, Long> {
    @Query(value="select e from EmailDelivery e where e.status = :status order by e.createdAt asc")
    public List<EmailDelivery> findTopByStatusOrderByCreatedAtAsc(String var1, Pageable var2);
}

