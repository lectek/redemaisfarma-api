package br.com.redemaisfarma.adapters.inbound.rest.v2;

import br.com.redemaisfarma.application.dto.request.CadastroProdutoRequestDTO;
import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController("produtoV2Controller")
@RequestMapping(value = "/api/v2/produtos", produces = "application/json")
@Validated
public class ProdutoV2Controller {

    private final ProdutoService produtoService;

    @Generated
    public ProdutoV2Controller(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // ==========================================================
    // Criar novo produto
    // ==========================================================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Produto> create(@Valid @RequestBody CadastroProdutoRequestDTO dto) {
        Produto salvo = produtoService.createFromDto(dto);
        URI location = URI.create("/api/v2/produtos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    // ==========================================================
    // Listar produtos
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<Produto>> list() {
        List<Produto> produtos = produtoService.list();
        return ResponseEntity.ok(produtos);
    }

    // ==========================================================
    // Buscar produto por ID
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Produto> findById(@PathVariable Long id) {
        Produto produto = produtoService.findById(id);
        return ResponseEntity.ok(produto);
    }

    // ==========================================================
    // Atualizar produto
    // ==========================================================
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Produto> update(
            @PathVariable Long id,
            @Valid @RequestBody CadastroProdutoRequestDTO dto
    ) {
        Produto tmp = produtoService.createFromDto(dto);
        tmp.setId(id);
        Produto atualizado = produtoService.update(id, tmp);
        return ResponseEntity.ok(atualizado);
    }

    // ==========================================================
    // Excluir produto
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
