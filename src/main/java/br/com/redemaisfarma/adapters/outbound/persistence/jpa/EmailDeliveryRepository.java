package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailDeliveryRepository extends JpaRepository<EmailDelivery, Long> {

    @Query("select e from EmailDelivery e where e.status = :status order by e.createdAt asc")
    List<EmailDelivery> findTopByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
