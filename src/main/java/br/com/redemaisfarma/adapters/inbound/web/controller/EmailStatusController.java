package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.dto.EmailDeliveryResponse;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/email")
public class EmailStatusController {

    private final EmailDeliveryRepository repo;

    public EmailStatusController(EmailDeliveryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailDeliveryResponse> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<EmailDeliveryResponse>> search(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String purpose,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 200),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

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

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        Page<EmailDelivery> pageResult = repo.findAll(Example.of(probe, matcher), pageable);
        Page<EmailDeliveryResponse> mapped = pageResult.map(this::toResponse);
        return ResponseEntity.ok(mapped);
    }

    @GetMapping(value = "/{id}/raw", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> raw(@PathVariable Long id) {
        return repo.findById(id)
                .map(EmailDelivery::getPayloadJson)
                .map(json -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retry(@PathVariable Long id) {
        Optional<EmailDelivery> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmailDelivery e = opt.get();
        if (!"FAILED".equalsIgnoreCase(e.getStatus())) {
            return ResponseEntity.unprocessableEntity()
                    .body("Somente registros com status=FAILED podem ser reprocessados.");
        }

        e.setStatus("PENDING");
        e.setLastError(null);
        e.setAttempts(0);
        repo.save(e);

        return ResponseEntity.accepted()
                .location(URI.create("/api/email/" + e.getId()))
                .build();
    }

    private EmailDeliveryResponse toResponse(EmailDelivery e) {
        return new EmailDeliveryResponse(
                e.getId(),
                e.getPurpose(),
                e.getDestination(),
                e.getProvider(),
                e.getStatus(),
                e.getAttempts(),
                e.getMessageId(),
                e.getLastError(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
