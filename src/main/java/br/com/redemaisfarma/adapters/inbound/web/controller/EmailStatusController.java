/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Example
 *  org.springframework.data.domain.ExampleMatcher
 *  org.springframework.data.domain.ExampleMatcher$StringMatcher
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.dto.EmailDeliveryResponse;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import java.net.URI;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value={"/api/email"})
public class EmailStatusController {
    private final EmailDeliveryRepository repo;

    public EmailStatusController(EmailDeliveryRepository repo) {
        this.repo = repo;
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<EmailDeliveryResponse> get(@PathVariable Long id) {
        return this.repo.findById(id).map(this::toResponse).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<EmailDeliveryResponse>> search(@RequestParam(required=false) String destination, @RequestParam(required=false) String status, @RequestParam(required=false) String purpose, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        PageRequest pageable = PageRequest.of((int)Math.max(page, 0), (int)Math.min(size, 200), (Sort)Sort.by((Sort.Direction)Sort.Direction.DESC, (String[])new String[]{"createdAt"}));
        EmailDelivery probe = new EmailDelivery();
        if (destination != null && !destination.isBlank()) {
            probe.setDestination(destination);
        }
        if (status != null && !status.isBlank()) {
            probe.setStatus(status);
        }
        if (purpose != null && !purpose.isBlank()) {
            probe.setPurpose(purpose);
        }
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Page pageResult = this.repo.findAll(Example.<br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery>of((Object)probe, (ExampleMatcher)matcher), (Pageable)pageable);
        Page mapped = pageResult.map(this::toResponse);
        return ResponseEntity.ok((Object)mapped);
    }

    @GetMapping(value={"/{id}/raw"})
    public ResponseEntity<String> raw(@PathVariable Long id) {
        return this.repo.findById(id).map(EmailDelivery::getPayloadJson).map(json -> ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Type", new String[]{"application/json"})).body(json)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value={"/{id}/retry"})
    public ResponseEntity<?> retry(@PathVariable Long id) {
        Optional opt = this.repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        EmailDelivery e = (EmailDelivery)opt.get();
        if (!"FAILED".equalsIgnoreCase(e.getStatus())) {
            return ResponseEntity.unprocessableEntity().body((Object)"Somente registros com status=FAILED podem ser reprocessados.");
        }
        e.setStatus("PENDING");
        e.setLastError(null);
        e.setAttempts(0);
        this.repo.save(e);
        return ((ResponseEntity.BodyBuilder)ResponseEntity.accepted().location(URI.create("/api/email/" + e.getId()))).build();
    }

    private EmailDeliveryResponse toResponse(EmailDelivery e) {
        return new EmailDeliveryResponse(e.getId(), e.getPurpose(), e.getDestination(), e.getProvider(), e.getStatus(), e.getAttempts(), e.getMessageId(), e.getLastError(), e.getCreatedAt(), e.getUpdatedAt());
    }
}

