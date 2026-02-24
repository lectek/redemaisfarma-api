package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.dto.request.EmailCampaignRequest;
import br.com.redemaisfarma.application.dto.request.EmailCampaignValidationRequest;
import br.com.redemaisfarma.application.dto.response.CampaignQueuePreviewResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignLogResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignQueueItemResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignResponse;
import br.com.redemaisfarma.application.dto.response.QueueDashboardResponse;
import br.com.redemaisfarma.application.dto.response.QueueStatusResponse;
import br.com.redemaisfarma.application.service.EmailCampaignService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Marketing de E-mail REST", description = "APIs REST para campanhas de e-mail marketing")
@RestController
@RequestMapping("/api/admin/marketing/emails/campanhas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailCampaignRestController {
    private final EmailCampaignService campaignService;

    public AdminMarketingEmailCampaignRestController(EmailCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "Lista campanhas paginadas")
    @GetMapping
    public Page<EmailCampaignResponse> list(Pageable pageable) {
        return campaignService.list(pageable);
    }

    @Operation(summary = "Consulta campanha pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> get(@PathVariable Long id) {
        return campaignService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria nova campanha")
    @PostMapping
    public ResponseEntity<EmailCampaignResponse> create(@RequestBody EmailCampaignRequest request) {
        EmailCampaignResponse response = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualiza campanha")
    @PutMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> update(@PathVariable Long id, @RequestBody EmailCampaignRequest request) {
        EmailCampaignResponse response = campaignService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancela uma campanha agendada")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        campaignService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pausa uma campanha agendada")
    @PostMapping("/{id}/pause")
    public ResponseEntity<EmailCampaignResponse> pause(@PathVariable Long id) {
        EmailCampaignResponse response = campaignService.pause(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retoma uma campanha pausada")
    @PostMapping("/{id}/resume")
    public ResponseEntity<EmailCampaignResponse> resume(@PathVariable Long id) {
        EmailCampaignResponse response = campaignService.resume(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualiza o status de validação da campanha")
    @PostMapping("/{id}/validate")
    public ResponseEntity<EmailCampaignResponse> validate(@PathVariable Long id,
                                                           @RequestBody EmailCampaignValidationRequest request) {
        EmailCampaignResponse response = campaignService.updateValidationStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Preview queue metrics filtered por segmento/periodo")
    @GetMapping("/{id}/preview/metrics")
    public CampaignQueuePreviewResponse previewMetrics(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant scheduledFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant scheduledTo,
            @RequestParam(required = false) String segmento
    ) {
        return campaignService.queuePreviewMetrics(id, status, scheduledFrom, scheduledTo, segmento);
    }

    @Operation(summary = "Gera o preview do template da campanha")
    @GetMapping(path = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.preview(id));
    }

    @Operation(summary = "Retorna o status da fila de campanhas")
    @GetMapping("/fila")
    public QueueDashboardResponse queue() {
        List<QueueStatusResponse> statuses = campaignService.queueStatus();
        List<EmailCampaignQueueItemResponse> queueItems = campaignService.latestQueueItems().stream()
                .map(this::mapQueueItem)
                .collect(Collectors.toList());
        List<EmailCampaignLogResponse> logs = campaignService.latestLogs().stream()
                .map(this::mapLog)
                .collect(Collectors.toList());
        return new QueueDashboardResponse(statuses, queueItems, logs);
    }

    private EmailCampaignQueueItemResponse mapQueueItem(EmailCampaignQueue queue) {
        EmailCampaignQueueItemResponse dto = new EmailCampaignQueueItemResponse();
        dto.setId(queue.getId());
        dto.setCampaignId(queue.getCampaignId());
        dto.setRecipientEmail(queue.getRecipientEmail());
        dto.setStatus(queue.getStatus());
        dto.setScheduledAt(queue.getScheduledAt());
        dto.setAttempts(queue.getAttempts());
        dto.setLastError(queue.getLastError());
        return dto;
    }

    private EmailCampaignLogResponse mapLog(EmailCampaignLog log) {
        EmailCampaignLogResponse dto = new EmailCampaignLogResponse();
        dto.setId(log.getId());
        dto.setCampaignId(log.getCampaignId());
        dto.setRecipientEmail(log.getRecipientEmail());
        dto.setStatus(log.getStatus());
        dto.setErrorText(log.getErrorText());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
