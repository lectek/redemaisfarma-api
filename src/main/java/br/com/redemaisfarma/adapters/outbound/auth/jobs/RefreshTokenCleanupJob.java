package br.com.redemaisfarma.adapters.outbound.auth.jobs;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Job de limpeza de refresh tokens expirados.
 *
 * Observações importantes: - Para funcionar, habilite agendamentos com @EnableScheduling (fora do outbound). - O cron
 * pode ser sobrescrito via propriedade: security.refresh.cleanup-cron
 */
@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private final RefreshTokenStore store;

    public RefreshTokenCleanupJob(RefreshTokenStore store) {
        this.store = store;
    }

    /**
     * Executa a cada 30 minutos por padrão. Formato: sec min hora diaMes mes diaSemana
     */
    @Scheduled(cron = "${security.refresh.cleanup-cron:0 0/30 * * * *}")
    public void cleanup() {
        Instant now = Instant.now();
        long removed = store.deleteExpired(now);
        if (removed > 0) {
            log.info("RefreshTokenCleanupJob: {} refresh tokens expirados removidos ({}).", removed, now);
        } else {
            log.debug("RefreshTokenCleanupJob: nenhum refresh token expirado para remover ({}).", now);
        }
    }
}
