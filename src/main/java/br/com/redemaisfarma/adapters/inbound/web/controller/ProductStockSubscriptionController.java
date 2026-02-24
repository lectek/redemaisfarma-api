package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.request.StockSubscriptionRequest;
import br.com.redemaisfarma.application.service.ProductStockSubscriptionService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/produtos/{produtoId}/stock")
@Tag(name = "App - Volta ao estoque", description = "Inscreve um e-mail para avisos de volta ao estoque")
public class ProductStockSubscriptionController {

    private final ProductStockSubscriptionService subscriptionService;

    public ProductStockSubscriptionController(ProductStockSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Inscrever e-mail para aviso quando o produto voltar ao estoque")
    public ResponseEntity<Void> subscribe(@PathVariable Long produtoId,
                                          @Valid @RequestBody StockSubscriptionRequest request) {
        ProductStockSubscriptionEntity entity = subscriptionService.subscribe(produtoId, request.getEmail(), request.getNome());
        URI location = URI.create("/api/public/produtos/" + produtoId + "/stock/subscriptions/" + entity.getId());
        return ResponseEntity.created(location).build();
    }
}
