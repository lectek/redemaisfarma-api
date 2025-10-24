/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Max
 *  jakarta.validation.constraints.Min
 *  lombok.Generated
 *  org.springframework.http.CacheControl
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.api.publico;

import br.com.redemaisfarma.adapters.inbound.web.ProdutoCardDTO;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(value={"/api/public/vitrine"})
public class VitrineApiController {
    private final ProdutoVitrineService vitrine;

    @GetMapping(value={"/destaques"})
    public ResponseEntity<List<ProdutoCardDTO>> destaques(@RequestParam(defaultValue="12") @Min(value=1L) @Max(value=50L) @Min(value=1L) @Max(value=50L) int limit) {
        List<ProdutoCardDTO> body = this.vitrine.listarDestaques(limit).stream().map(ProdutoCardDTO::from).toList();
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().cacheControl(CacheControl.maxAge((long)5L, (TimeUnit)TimeUnit.MINUTES).cachePublic())).body(body);
    }

    @Generated
    public VitrineApiController(ProdutoVitrineService vitrine) {
        this.vitrine = vitrine;
    }
}

