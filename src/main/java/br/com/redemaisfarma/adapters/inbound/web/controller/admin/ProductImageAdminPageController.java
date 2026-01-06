package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImagePublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.service.ProductImageJobService;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Profile("!test")
@Controller
@RequestMapping
public class ProductImageAdminPageController {

    private final ProdutoJpaRepository produtoRepo;
    private final ProductImagePublisher publisher;
    private final ProductImageJobService jobService;
    private final ProductImageJobRepository jobRepo;

    @Generated
    public ProductImageAdminPageController(
            ProdutoJpaRepository produtoRepo,
            ProductImagePublisher publisher,
            ProductImageJobService jobService,
            ProductImageJobRepository jobRepo
    ) {
        this.produtoRepo = produtoRepo;
        this.publisher = publisher;
        this.jobService = jobService;
        this.jobRepo = jobRepo;
    }

    @GetMapping("/admin/imagens")
    public String pageProdutosSemImagem(Pageable pageable, Model model) {
        Page<ProdutoEntity> page = produtoRepo.findSemMidia(pageable);
        model.addAttribute("page", page);
        model.addAttribute("totalSemImagem", page.getTotalElements());
        return "pages/admin/imagens/index";
    }

    @GetMapping("/api/admin/imagens/produtos/sem-imagem")
    @ResponseBody
    public Page<ProdutoEntity> apiProdutosSemImagem(Pageable pageable) {
        return produtoRepo.findSemMidia(pageable);
    }

    @PostMapping("/api/admin/imagens/{produtoId}/queue")
    @ResponseBody
    public ResponseEntity<?> queue(@PathVariable Long produtoId) {
        var produto = produtoRepo.findById(produtoId).orElse(null);
        if (produto == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Produto não encontrado");
        }

        publisher.publish(new ProductImageRequestedEvent(
                produto.getId(),
                produto.getNome(),
                produto.getFabricante(),
                produto.getCategoria(),
                String.valueOf(produto.getId())
        ));

        var lastJob = jobRepo.findLastByProduct(produtoId).orElse(null);
        return ResponseEntity.ok(new EnqueueResponse("ENFILEIRADO",
                lastJob == null ? null : JobView.from(lastJob)));
    }

    @PostMapping("/api/admin/imagens/{produtoId}/regenerate")
    @ResponseBody
    public ResponseEntity<?> regenerate(@PathVariable Long produtoId) {
        jobService.regenerateForced(produtoId);
        var lastJob = jobRepo.findLastByProduct(produtoId).orElse(null);
        return ResponseEntity.ok(new EnqueueResponse("REGERADO",
                lastJob == null ? null : JobView.from(lastJob)));
    }

    @GetMapping("/api/admin/imagens/jobs")
    @ResponseBody
    public List<JobView> jobs(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        ProductImageJobRepository.Status st;
        if (status == null || status.isBlank()) {
            st = ProductImageJobRepository.Status.QUEUED;
        } else {
            st = ProductImageJobRepository.Status.valueOf(status.toUpperCase());
        }

        return jobRepo.findByStatus(
                        st,
                        Math.max(1, Math.min(limit, 1000)),
                        Math.max(0, offset))
                .stream()
                .map(JobView::from)
                .toList();
    }

    // ===============================
    // Records auxiliares
    // ===============================

    public record EnqueueResponse(String result, JobView lastJob) {}

    public record JobView(
            Long id,
            Long productId,
            String status,
            String resultUrl,
            String errorMsg,
            String fingerprint,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static JobView from(ProductImageJobRepository.Job j) {
            return new JobView(
                    j.id(),
                    j.productId(),
                    j.status().name(),
                    j.resultUrl(),
                    j.errorMsg(),
                    j.fingerprint(),
                    j.createdAt(),
                    j.updatedAt()
            );
        }
    }
}
