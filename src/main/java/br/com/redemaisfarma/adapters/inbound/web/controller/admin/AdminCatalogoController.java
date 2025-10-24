/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/admin/catalogo"})
public class AdminCatalogoController {
    private final SincronizacaoCatalogoService syncService;

    @PostMapping(value={"/sincronizar"})
    public ResponseEntity<SincronizacaoCatalogoService.ResumoSync> sincronizar() {
        SincronizacaoCatalogoService.ResumoSync resumo = this.syncService.sincronizarTudo();
        return ResponseEntity.ok((Object)resumo);
    }

    @Generated
    public AdminCatalogoController(SincronizacaoCatalogoService syncService) {
        this.syncService = syncService;
    }
}

