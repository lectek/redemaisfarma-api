/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.ResponseEntity
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.ResponseStatus
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController(value="clienteAppController")
@RequestMapping(value={"/api/app/clientes"}, produces={"application/json"})
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping(consumes={"application/json"})
    public ResponseEntity<Cliente> create(@Valid @RequestBody Cliente cliente) {
        Cliente salvo = this.clienteService.create(cliente);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{salvo.getId()}).toUri();
        return ResponseEntity.created((URI)location).body((Object)salvo);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> list() {
        List<Cliente> clientes = this.clienteService.list();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<Cliente> findById(@PathVariable Long id) {
        Cliente cliente = this.clienteService.findById(id);
        return ResponseEntity.ok((Object)cliente);
    }

    @PutMapping(value={"/{id}"}, consumes={"application/json"})
    public ResponseEntity<Cliente> update(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        cliente.setId(id);
        Cliente atualizado = this.clienteService.update(id, cliente);
        return ResponseEntity.ok((Object)atualizado);
    }

    @DeleteMapping(value={"/{id}"})
    @ResponseStatus(value=HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        this.clienteService.delete(id);
    }
}

