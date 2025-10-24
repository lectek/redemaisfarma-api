/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
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

import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.Generated;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

@RestController(value="pedidoV2Controller")
@RequestMapping(value={"/api/v2/pedidos"}, produces={"application/json"}, consumes={"application/json"})
@Validated
public class PedidoV2Controller {
    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody @Valid Pedido pedido) {
        Pedido salvo = this.pedidoService.create(pedido);
        URI location = URI.create("/api/v2/pedidos/" + salvo.getId());
        return ResponseEntity.created((URI)location).body((Object)salvo);
    }

    @GetMapping(value={"/{id}"}, consumes={"*/*"})
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return this.pedidoService.findByIdOptional(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(consumes={"*/*"})
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(this.pedidoService.list());
    }

    @PutMapping(value={"/{id}"})
    public ResponseEntity<Pedido> atualizar(@PathVariable Long id, @RequestBody @Valid Pedido pedido) {
        Pedido atualizado = this.pedidoService.update(id, pedido);
        return ResponseEntity.ok((Object)atualizado);
    }

    @DeleteMapping(value={"/{id}"}, consumes={"*/*"})
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        this.pedidoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public PedidoV2Controller(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }
}

