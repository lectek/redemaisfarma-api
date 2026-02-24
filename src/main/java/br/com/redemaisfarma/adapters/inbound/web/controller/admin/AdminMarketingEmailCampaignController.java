package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.MailService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin - Marketing de E-mail", description = "Campanhas e templates de e-mail marketing do admin")
@Controller
@RequestMapping("/admin/marketing/emails/campanhas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailCampaignController {
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String QUEUE_PENDING = "PENDING";
    private static final String QUEUE_SENDING = "SENDING";
    private static final String QUEUE_CANCELLED = "CANCELLED";
    private static final List<String> QUEUE_STATUS_ORDER = List.of("PENDING", "SENDING", "SENT", "FAILED", "CANCELLED");

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final EmailCampaignLogRepository logRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    public AdminMarketingEmailCampaignController(
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            EmailCampaignLogRepository logRepository,
            ClienteRepository clienteRepository,
            UsuarioJpaRepository usuarioRepository,
            MailService mailService,
            ObjectMapper objectMapper
    ) {
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.mailService = mailService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Exibe o painel de monitoramento da fila de campanhas")
    @GetMapping("/fila")
    public String fila(Model model) {
        Map<String, Long> totals = queueRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        EmailCampaignQueueRepository.StatusCount::getStatus,
                        EmailCampaignQueueRepository.StatusCount::getTotal
                ));
        List<QueueStatus> statuses = QUEUE_STATUS_ORDER.stream()
                .map(status -> new QueueStatus(status, totals.getOrDefault(status, 0L)))
                .toList();
        List<EmailCampaignQueue> queueItems = queueRepository.findTop20ByOrderByCreatedAtDesc();
        Set<Long> campaignIds = queueItems.stream()
                .map(EmailCampaignQueue::getCampaignId)
                .collect(Collectors.toSet());
        Map<Long, String> campaignNames = campaignRepository.findAllById(campaignIds).stream()
                .collect(Collectors.toMap(EmailCampaign::getId, EmailCampaign::getNome));
        List<EmailCampaignLog> logs = logRepository.findTop20ByOrderByCreatedAtDesc();

        model.addAttribute("statuses", statuses);
        model.addAttribute("queueItems", queueItems);
        model.addAttribute("campaignNames", campaignNames);
        model.addAttribute("logs", logs);
        return "pages/admin/marketing/emails/fila";
    }

    @Operation(summary = "Exibe o formulário de campanhas com templates e campanhas existentes")
    @GetMapping
    public String form(Model model) {
        model.addAttribute("campanha", new CampanhaForm());
        model.addAttribute("templates", templateOptions());
        model.addAttribute("campanhas", campaignRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "pages/admin/marketing/emails/camapnhas";
    }

    @Operation(summary = "Renderiza o preview do template usado pela campanha")
    @GetMapping(path = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String preview(@PathVariable Long id) {
        EmailCampaign campaign = findCampaign(id);
        String segmento = extractSegment(campaign);
        Map<String, Object> model = buildPayloadModel(campaign.getNome(), segmento);
        return mailService.renderTemplate(campaign.getTemplateKey(), model);
    }

    @Operation(summary = "Cria uma nova campanha de e-mail e gera a fila quando agendada")
    @PostMapping
    public String criar(@ModelAttribute("campanha") CampanhaForm form, RedirectAttributes ra) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome(safe(form.getNome()));
        campaign.setAssunto(safe(form.getAssunto()));
        campaign.setTemplateKey(safe(form.getTemplateKey()));
        campaign.setSegmentJson(buildSegmentJson(form));
        Instant scheduleAt = resolveSchedule(form);
        campaign.setScheduledAt(scheduleAt);
        campaign.setScheduledZone(ZoneId.systemDefault().getId());
        campaign.setValidationStatus(QUEUE_PENDING);
        campaign.setStatus(scheduleAt == null ? STATUS_DRAFT : STATUS_SCHEDULED);
        campaignRepository.save(campaign);

        if (scheduleAt != null) {
            int queued = enqueueAllClientes(campaign, scheduleAt, form);
            if (queued == 0) {
                ra.addFlashAttribute("warning", "Campanha criada, mas nenhum cliente ativo encontrado.");
            } else {
                ra.addFlashAttribute("success", "Campanha criada e fila gerada (" + queued + ").");
            }
        } else {
            ra.addFlashAttribute("success", "Campanha criada como rascunho.");
        }
        return "redirect:/admin/marketing/emails/campanhas";
    }

    @PostMapping("/{id}/cancel")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        EmailCampaign campaign = findCampaign(id);
        campaign.setStatus(STATUS_CANCELLED);
        campaignRepository.save(campaign);
        List<EmailCampaignQueue> queued = queueRepository.findByCampaignIdAndStatusIn(
                campaign.getId(),
                List.of(QUEUE_PENDING, QUEUE_SENDING)
        );
        queued.forEach(item -> item.setStatus(QUEUE_CANCELLED));
        queueRepository.saveAll(queued);
        ra.addFlashAttribute("success", "Campanha cancelada e fila atualizada (" + queued.size() + ").");
        return "redirect:/admin/marketing/emails/campanhas";
    }

    private Instant resolveSchedule(CampanhaForm form) {
        if (Boolean.TRUE.equals(form.getEnvioImediato())) {
            return Instant.now();
        }
        LocalDateTime dt = form.getAgendarPara();
        if (dt == null) {
            return null;
        }
        return dt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private int enqueueAllClientes(EmailCampaign campaign, Instant scheduledAt, CampanhaForm form) {
        List<Recipient> recipients = resolveRecipients(form);
        int total = 0;
        for (Recipient recipient : recipients) {
            EmailCampaignQueue queue = new EmailCampaignQueue();
            queue.setCampaignId(campaign.getId());
            queue.setRecipientEmail(recipient.email());
            queue.setRecipientName(recipient.name());
            queue.setStatus(QUEUE_PENDING);
            queue.setScheduledAt(scheduledAt);
            queue.setPayloadJson(buildPayload(campaign.getNome(), SegmentType.from(form.getSegmento()).name()));
            queueRepository.save(queue);
            total++;
        }
        return total;
    }

    private List<Recipient> resolveRecipients(CampanhaForm form) {
        SegmentType type = SegmentType.from(form.getSegmento());
        switch (type) {
            case VIP -> {
                return resolveVipRecipients();
            }
            case INATIVOS_90D -> {
                LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
                List<ClienteEntity> inativos = clienteRepository.findInativosAntesDe(cutoff);
                return toRecipients(inativos);
            }
            case CATEGORIA -> {
                List<ClienteEntity> byCategoria = resolveCategoriaRecipients(form.getCategoria());
                return toRecipients(byCategoria);
            }
            case RECENCIA -> {
                List<ClienteEntity> byRecencia = resolveRecencyRecipients(form.getRecenciaDias());
                return toRecipients(byRecencia);
            }
            case TICKET -> {
                List<ClienteEntity> byTicket = resolveTicketRecipients(form.getTicketMinimo());
                return toRecipients(byTicket);
            }
            default -> {
                return toRecipients(clienteRepository.findAll());
            }
        }
    }

    private List<Recipient> resolveVipRecipients() {
        List<Recipient> out = new ArrayList<>();
        usuarioRepository.findByClienteVipTrue().forEach(usuario -> {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                return;
            }
            clienteRepository.findByEmailIgnoreCase(usuario.getEmail())
                    .filter(ClienteEntity::isAtivo)
                    .ifPresentOrElse(
                            cliente -> out.add(new Recipient(cliente.getEmail(), cliente.getNome())),
                            () -> out.add(new Recipient(usuario.getEmail(), usuario.getNome()))
                    );
        });
        return out;
    }

    private List<ClienteEntity> resolveCategoriaRecipients(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return List.of();
        }
        return clienteRepository.findClientesByCategoriaComprada(categoria.trim(), StatusPedido.CANCELADO);
    }

    private List<ClienteEntity> resolveRecencyRecipients(Integer dias) {
        if (dias == null || dias <= 0) {
            return List.of();
        }
        LocalDateTime from = LocalDateTime.now().minusDays(dias);
        return clienteRepository.findClientesByRecencia(from, StatusPedido.CANCELADO);
    }

    private List<ClienteEntity> resolveTicketRecipients(BigDecimal ticketMinimo) {
        if (ticketMinimo == null || ticketMinimo.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return clienteRepository.findClientesByTicketMedio(ticketMinimo, StatusPedido.CANCELADO);
    }

    private List<Recipient> toRecipients(List<ClienteEntity> clientes) {
        List<Recipient> out = new ArrayList<>();
        for (ClienteEntity cliente : clientes) {
            if (cliente == null || !cliente.isAtivo()) {
                continue;
            }
            if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                continue;
            }
            out.add(new Recipient(cliente.getEmail(), cliente.getNome()));
        }
        return out;
    }

    private String buildPayload(String campaignName, String segmento) {
        try {
            return objectMapper.writeValueAsString(buildPayloadModel(campaignName, segmento));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> buildPayloadModel(String campaignName, String segmento) {
        return Map.of(
                "headline", campaignName == null ? "" : campaignName,
                "message", "Confira as ofertas selecionadas para voce.",
                "segmento", segmento == null ? "" : segmento
        );
    }

    private String extractSegment(EmailCampaign campaign) {
        if (campaign.getSegmentJson() == null || campaign.getSegmentJson().isBlank()) {
            return "";
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(campaign.getSegmentJson(), Map.class);
            Object segmento = parsed.get("segmento");
            return segmento == null ? "" : segmento.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private EmailCampaign findCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campanha não encontrada"));
    }

    private String buildSegmentJson(CampanhaForm form) {
        SegmentType segmentType = SegmentType.from(form.getSegmento());
        Map<String, Object> payload = new HashMap<>();
        payload.put("segmento", segmentType.name());
        if (form.getCategoria() != null && !form.getCategoria().isBlank()) {
            payload.put("categoria", form.getCategoria().trim());
        }
        if (form.getRecenciaDias() != null) {
            payload.put("recenciaDias", form.getRecenciaDias());
        }
        if (form.getTicketMinimo() != null) {
            payload.put("ticketMinimo", form.getTicketMinimo());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private List<TemplateOption> templateOptions() {
        return List.of(
                new TemplateOption("mail/promo", "Promo - basico"),
                new TemplateOption("mail/order-status", "Pedido - status")
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private enum SegmentType {
        TODOS,
        VIP,
        INATIVOS_90D,
        CATEGORIA,
        RECENCIA,
        TICKET;

        static SegmentType from(String raw) {
            if (raw == null) {
                return TODOS;
            }
            try {
                return SegmentType.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return TODOS;
            }
        }
    }

    public static class CampanhaForm {
        private String nome;
        private String assunto;
        private String templateKey;
        private String segmento;
        private String categoria;
        private Integer recenciaDias;
        private BigDecimal ticketMinimo;
        private Boolean envioImediato;
        private LocalDateTime agendarPara;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getAssunto() {
            return assunto;
        }

        public void setAssunto(String assunto) {
            this.assunto = assunto;
        }

        public String getTemplateKey() {
            return templateKey;
        }

        public void setTemplateKey(String templateKey) {
            this.templateKey = templateKey;
        }

        public String getSegmento() {
            return segmento;
        }

        public void setSegmento(String segmento) {
            this.segmento = segmento;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public Integer getRecenciaDias() {
            return recenciaDias;
        }

        public void setRecenciaDias(Integer recenciaDias) {
            this.recenciaDias = recenciaDias;
        }

        public BigDecimal getTicketMinimo() {
            return ticketMinimo;
        }

        public void setTicketMinimo(BigDecimal ticketMinimo) {
            this.ticketMinimo = ticketMinimo;
        }

        public Boolean getEnvioImediato() {
            return envioImediato;
        }

        public void setEnvioImediato(Boolean envioImediato) {
            this.envioImediato = envioImediato;
        }

        public LocalDateTime getAgendarPara() {
            return agendarPara;
        }

        public void setAgendarPara(LocalDateTime agendarPara) {
            this.agendarPara = agendarPara;
        }
    }

    public record TemplateOption(String key, String label) {
    }

    public record Recipient(String email, String name) {
    }

    public record QueueStatus(String status, long total) {
    }
}
