// src/main/java/br/com/redemaisfarma/application/service/otp/OtpCleanupJob.java
package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OtpCleanupJob {

  private static final Logger log = LoggerFactory.getLogger(OtpCleanupJob.class);
  private final OtpCodeRepository repo;

  public OtpCleanupJob(OtpCodeRepository repo) {
    this.repo = repo;
  }

  @Scheduled(fixedDelayString = "${otp.cleanup.fixed-delay-ms:300000}")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void run() {
    try {
      long removed = repo.deleteByExpiresAtBefore(Instant.now()); // <- long
      if (removed > 0) {
        log.info("otp_cleanup: removidos {}", removed);
      } else {
        log.debug("otp_cleanup: nenhum registro expirado para remover");
      }
    } catch (Exception e) {
      log.error("otp_cleanup: falha ao remover registros expirados", e);
    }
  }
}
