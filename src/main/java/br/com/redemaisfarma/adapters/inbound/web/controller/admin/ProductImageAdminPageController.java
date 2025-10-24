/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.ResponseBody
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImagePublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.service.ProductImageJobService;
import java.time.Instant;
import java.util.List;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Profile(value={"!test"})
@Controller
@RequestMapping
public class ProductImageAdminPageController {
    private final ProdutoJpaRepository produtoRepo;
    private final ProductImagePublisher publisher;
    private final ProductImageJobService jobService;
    private final ProductImageJobRepository jobRepo;

    @GetMapping(value={"/admin/imagens"})
    public String pageProdutosSemImagem(Pageable pageable, Model model) {
        Page<ProdutoEntity> page = this.produtoRepo.findSemMidia(pageable);
        model.addAttribute("page", page);
        model.addAttribute("totalSemImagem", (Object)page.getTotalElements());
        return "admin/imagens/index";
    }

    @GetMapping(value={"/api/admin/imagens/produtos/sem-imagem"})
    @ResponseBody
    public Page<ProdutoEntity> apiProdutosSemImagem(Pageable pageable) {
        return this.produtoRepo.findSemMidia(pageable);
    }

    @PostMapping(value={"/api/admin/imagens/{produtoId}/queue"})
    @ResponseBody
    public ResponseEntity<?> queue(@PathVariable Long produtoId) {
        ProdutoEntity p = this.produtoRepo.findById(produtoId).orElse(null);
        if (p == null) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).body((Object)"Produto n\u00e3o encontrado");
        }
        this.publisher.publish(new ProductImageRequestedEvent(p.getId(), p.getNome(), p.getFabricante(), p.getCategoria(), String.valueOf(p.getId())));
        ProductImageJobRepository.Job last = this.jobRepo.findLastByProduct(produtoId).orElse(null);
        return ResponseEntity.ok((Object)new EnqueueResponse("ENFILEIRADO", last == null ? null : JobView.from(last)));
    }

    @PostMapping(value={"/api/admin/imagens/{produtoId}/regenerate"})
    @ResponseBody
    public ResponseEntity<?> regenerate(@PathVariable Long produtoId) {
        this.jobService.regenerateForced(produtoId);
        ProductImageJobRepository.Job last = this.jobRepo.findLastByProduct(produtoId).orElse(null);
        return ResponseEntity.ok((Object)new EnqueueResponse("REGERADO", last == null ? null : JobView.from(last)));
    }

    @GetMapping(value={"/api/admin/imagens/jobs"})
    @ResponseBody
    public List<JobView> jobs(@RequestParam(name="status", required=false) String status, @RequestParam(defaultValue="20") int limit, @RequestParam(defaultValue="0") int offset) {
        ProductImageJobRepository.Status st = null;
        if (status != null && !status.isBlank()) {
            st = ProductImageJobRepository.Status.valueOf(status.toUpperCase());
        }
        if (st == null) {
            st = ProductImageJobRepository.Status.QUEUED;
        }
        return this.jobRepo.findByStatus(st, Math.max(1, Math.min(limit, 1000)), Math.max(0, offset)).stream().map(JobView::from).toList();
    }

    @Generated
    public ProductImageAdminPageController(ProdutoJpaRepository produtoRepo, ProductImagePublisher publisher, ProductImageJobService jobService, ProductImageJobRepository jobRepo) {
        this.produtoRepo = produtoRepo;
        this.publisher = publisher;
        this.jobService = jobService;
        this.jobRepo = jobRepo;
    }

    public record EnqueueResponse(String result, JobView lastJob) {
    }

    public record JobView(Long id, Long productId, String status, String resultUrl, String errorMsg, String fingerprint, Instant createdAt, Instant updatedAt) {
        public static JobView from(ProductImageJobRepository.Job j) {
            return new JobView(j.id(), j.productId(), j.status().name(), j.resultUrl(), j.errorMsg(), j.fingerprint(), j.createdAt(), j.updatedAt());
        }
    }
}

