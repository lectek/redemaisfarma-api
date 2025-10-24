/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.inbound.scheduler;

import br.com.redemaisfarma.application.service.sync.ProdutoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name={"legacy.sync.enabled"}, havingValue="true", matchIfMissing=false)
@ConditionalOnBean(value={ProdutoSyncService.class})
public class SincronizarProdutosJob {
    private static final Logger log = LoggerFactory.getLogger(SincronizarProdutosJob.class);
    private final ProdutoSyncService produtoSyncService;

    public SincronizarProdutosJob(ProdutoSyncService produtoSyncService) {
        this.produtoSyncService = produtoSyncService;
    }

    @Scheduled(cron="0 0 * * * *")
    public void executar() {
        log.info("Iniciando sincroniza\u00e7\u00e3o de produtos\u2026");
        try {
            int total = this.produtoSyncService.sincronizar();
            log.info("Sincroniza\u00e7\u00e3o conclu\u00edda. Produtos processados: {}", (Object)total);
        }
        catch (Exception e) {
            log.error("Erro na sincroniza\u00e7\u00e3o de produtos", (Throwable)e);
        }
    }
}

