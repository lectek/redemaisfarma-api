package br.com.redemaisfarma.application.port.outbound;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductImageJobRepository {

    enum Status { QUEUED, RUNNING, DONE, ERROR, SKIPPED }

    record Job(
        Long id,
        Long productId,
        Status status,
        String resultUrl,
        String errorMsg,
        String fingerprint,
        Instant createdAt,
        Instant updatedAt
    ) {}

    Job createQueued(Long productId, String fingerprint);
    void markRunning(Long jobId);
    void markDone(Long jobId, String resultUrl);
    void markError(Long jobId, String errorMsg);
    void markSkipped(Long jobId, String reason);

    Optional<Job> findLastByProduct(Long productId);
    List<Job> findByStatus(Status status, int limit, int offset);
    boolean existsByProductIdAndFingerprint(Long productId, String fingerprint);
}
