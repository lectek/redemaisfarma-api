package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailCampaignQueueRepository extends JpaRepository<EmailCampaignQueue, Long> {
    List<EmailCampaignQueue> findByStatusOrderByScheduledAtAsc(String status, Pageable pageable);

    @Query("""
        select q
        from EmailCampaignQueue q
        where q.status = :status
          and (q.scheduledAt is null or q.scheduledAt <= :now)
        order by q.scheduledAt asc, q.createdAt asc
        """)
    List<EmailCampaignQueue> findReady(@Param("status") String status, @Param("now") Instant now, Pageable pageable);
}
