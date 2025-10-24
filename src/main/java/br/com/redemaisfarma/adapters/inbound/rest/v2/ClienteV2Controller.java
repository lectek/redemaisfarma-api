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

import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.domain.Cliente;
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

@RestController(value="clienteV2Controller")
@RequestMapping(value={"/api/v2/clientes"})
@Validated
public class ClienteV2Controller {
    private final ClienteService clienteService;

    @PostMapping(consumes={"application/json"}, produces={"application/json"})
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente cliente) {
        Cliente salvo = this.clienteService.create(cliente);
        URI location = URI.create("/api/v2/clientes/" + salvo.getId());
        return ResponseEntity.created((URI)location).body((Object)salvo);
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        Cliente c = this.clienteService.findById(id);
        return ResponseEntity.ok((Object)c);
    }

    @GetMapping(produces={"application/json"})
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(this.clienteService.list());
    }

    @PutMapping(value={"/{id}"}, consumes={"application/json"}, produces={"application/json"})
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @RequestBody @Valid Cliente cliente) {
        Cliente atualizado = this.clienteService.update(id, cliente);
        return ResponseEntity.ok((Object)atualizado);
    }

    @DeleteMapping(value={"/{id}"})
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        this.clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public ClienteV2Controller(ClienteService clienteService) {
        this.clienteService = clienteService;
    }
}

