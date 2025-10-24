/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.rest.v2;

import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="produtoV2Controller")
@RequestMapping(value={"/api/v2/produtos"})
@Validated
public class ProdutoV2Controller {
    private final ProdutoService produtoService;

    @PostMapping(consumes={"application/json"}, produces={"application/json"})
    public ResponseEntity<Produto> create(@RequestBody @Valid CadastroProdutoRequestDTO dto) {
        Produto salvo = this.produtoService.createFromDto(dto);
        URI location = URI.create("/api/v2/produtos/" + salvo.getId());
        return ResponseEntity.created((URI)location).body((Object)salvo);
    }

    @GetMapping(produces={"application/json"})
    public ResponseEntity<List<Produto>> list() {
        return ResponseEntity.ok(this.produtoService.list());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    public ResponseEntity<Produto> findById(@PathVariable Long id) {
        Produto p = this.produtoService.findById(id);
        return ResponseEntity.ok((Object)p);
    }

    @PutMapping(value={"/{id}"}, consumes={"application/json"}, produces={"application/json"})
    public ResponseEntity<Produto> update(@PathVariable Long id, @RequestBody @Valid CadastroProdutoRequestDTO dto) {
        Produto tmp = this.produtoService.createFromDto(dto);
        tmp.setId(id);
        Produto atualizado = this.produtoService.update(id, tmp);
        return ResponseEntity.ok((Object)atualizado);
    }

    @DeleteMapping(value={"/{id}"})
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public ProdutoV2Controller(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }
}

