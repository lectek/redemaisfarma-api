package br.com.redemaisfarma.adapters.outbound.persistence.adapter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductImageJobEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProductImageJobJpaRepository;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductImageJobRepositoryAdapter implements ProductImageJobRepository {

    private final ProductImageJobJpaRepository jpa;

    public ProductImageJobRepositoryAdapter(ProductImageJobJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Job createQueued(Long productId, String fingerprint) {
        var e = new ProductImageJobEntity();
        e.setProductId(productId);
        e.setStatus(Status.QUEUED.name());
        e.setFingerprint(fingerprint);
        // createdAt/updatedAt são tratados pela base (default/on update) ou @PrePersist
        e = jpa.save(e);
        return map(e);
    }

    @Override @Transactional public void markRunning(Long jobId) { update(jobId, Status.RUNNING, null, null); }
    @Override @Transactional public void markDone(Long jobId, String resultUrl) { update(jobId, Status.DONE, resultUrl, null); }
    @Override @Transactional public void markError(Long jobId, String errorMsg) { update(jobId, Status.ERROR, null, errorMsg); }
    @Override @Transactional public void markSkipped(Long jobId, String reason) { update(jobId, Status.SKIPPED, null, reason); }

    @Override
    public Optional<Job> findLastByProduct(Long productId) {
        var e = jpa.findTopByProductIdOrderByCreatedAtDesc(productId);
        return Optional.ofNullable(e).map(this::map);
    }

    @Override
    public List<Job> findByStatus(Status status, int limit, int offset) {
        int size = Math.max(1, limit);
        int pageIndex = Math.max(0, offset / size);
        var pageReq = PageRequest.of(pageIndex, size);
        return jpa.findByStatusOrderByCreatedAtAsc(status.name(), pageReq)
                  .getContent()     // <- usa Page do Spring Data
                  .stream()
                  .map(this::map)
                  .toList();
    }

    @Override
    public boolean existsByProductIdAndFingerprint(Long productId, String fingerprint) {
        return jpa.existsByProductIdAndFingerprint(productId, fingerprint);
    }

    // ---- internals ----
    private void update(Long id, Status st, String url, String err) {
        var e = jpa.findById(id).orElseThrow();
        e.setStatus(st.name());
        if (url != null) e.setResultUrl(url);
        if (err != null) e.setErrorMsg(err);
        // updatedAt fica a cargo do banco (ON UPDATE) ou @PreUpdate caso você adicione
        jpa.save(e);
    }

    private Job map(ProductImageJobEntity e) {
        Status st;
        try { st = (e.getStatus() != null) ? Status.valueOf(e.getStatus()) : Status.QUEUED; }
        catch (IllegalArgumentException ex) { st = Status.ERROR; }

        return new Job(
            e.getId(),
            e.getProductId(),
            st,
            e.getResultUrl(),
            e.getErrorMsg(),
            e.getFingerprint(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
