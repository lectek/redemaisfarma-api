/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.admin;

import br.com.redemaisfarma.application.service.SincronizacaoCatalogoService;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/admin/export"})
public class AdminExportController {
    private final SincronizacaoCatalogoService sync;

    @PostMapping(value={"/produtos/full"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public ResponseEntity<SincronizacaoCatalogoService.ResumoSync> runFull() {
        return ResponseEntity.ok((Object)this.sync.sincronizarTudo());
    }

    @GetMapping(value={"/produtos/full"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public ResponseEntity<SincronizacaoCatalogoService.ResumoSync> runFullGet() {
        return ResponseEntity.ok((Object)this.sync.sincronizarTudo());
    }

    @Generated
    public AdminExportController(SincronizacaoCatalogoService sync) {
        this.sync = sync;
    }
}

