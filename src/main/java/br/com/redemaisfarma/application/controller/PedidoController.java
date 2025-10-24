/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.CrossOrigin
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import java.util.List;
import lombok.Generated;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"${api.base-path:/api}/${api.version:v2}/pedidos"})
@Validated
@CrossOrigin(origins={"*"})
public class PedidoController {
    private final PedidoService service;

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        Pedido criado = this.service.create(pedido);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body((Object)criado);
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(this.service.list());
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return this.service.findByIdOptional(id).map(ResponseEntity::ok).orElse(ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping(value={"/{id}"})
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public PedidoController(PedidoService service) {
        this.service = service;
    }
}

