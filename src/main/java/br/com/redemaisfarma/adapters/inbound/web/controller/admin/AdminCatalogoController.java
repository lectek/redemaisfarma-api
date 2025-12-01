package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/catalogo", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin - Catálogo", description = "Operações administrativas de sincronização do catálogo")
public class AdminCatalogoController {

    private final SincronizacaoCatalogoService syncService;

    /**
     * Dispara a sincronização completa do catálogo.
     * Protegido para ADMIN. Retorna um resumo da execução.
     */
    @PostMapping(path = "/sincronizar")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN:CATALOGO:SINCRONIZAR')")
    @Operation(summary = "Sincronizar catálogo",
               description = "Executa o processo de sincronização completa do catálogo e retorna um resumo da execução.")
    public ResponseEntity<SincronizacaoCatalogoService.ResumoSync> sincronizar() {
        StopWatch sw = new StopWatch("sincronizar-catalogo");
        sw.start();
        try {
            log.info("Iniciando sincronização de catálogo (trigger manual /admin/catalogo/sincronizar)...");
            SincronizacaoCatalogoService.ResumoSync resumo = syncService.sincronizarTudo();
            sw.stop();
            log.info("Sincronização de catálogo finalizada em {} ms. Resultado: {}", sw.getTotalTimeMillis(), resumo);
            return ResponseEntity.ok(resumo);
        } catch (IllegalArgumentException e) {
            // será traduzido pelo seu RestExceptionTranslator (400)
            sw.stop();
            log.warn("Falha de validação na sincronização do catálogo após {} ms: {}", sw.getTotalTimeMillis(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // será traduzido pelo seu RestExceptionTranslator (500)
            if (sw.isRunning()) sw.stop();
            log.error("Erro inesperado na sincronização do catálogo após {} ms", sw.getTotalTimeMillis(), e);
            throw e;
        }
    }
}
