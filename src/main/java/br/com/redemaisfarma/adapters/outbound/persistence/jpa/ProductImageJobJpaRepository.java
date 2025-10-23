package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductImageJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJobJpaRepository extends JpaRepository<ProductImageJobEntity, Long> {
    Page<ProductImageJobEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
    ProductImageJobEntity findTopByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByProductIdAndFingerprint(Long productId, String fingerprint);
}
