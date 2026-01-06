package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/marketing/emails/campanhas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailCampaignController {
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String QUEUE_PENDING = "PENDING";

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public AdminMarketingEmailCampaignController(
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            ClienteRepository clienteRepository,
            UsuarioJpaRepository usuarioRepository,
            ObjectMapper objectMapper
    ) {
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("campanha", new CampanhaForm());
        model.addAttribute("templates", templateOptions());
        model.addAttribute("campanhas", campaignRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "pages/admin/marketing/emails/camapnhas";
    }

    @PostMapping
    public String criar(@ModelAttribute("campanha") CampanhaForm form, RedirectAttributes ra) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome(safe(form.getNome()));
        campaign.setAssunto(safe(form.getAssunto()));
        campaign.setTemplateKey(safe(form.getTemplateKey()));
        campaign.setSegmentJson(buildSegmentJson(form.getSegmento()));
        Instant scheduleAt = resolveSchedule(form);
        campaign.setScheduledAt(scheduleAt);
        campaign.setStatus(scheduleAt == null ? STATUS_DRAFT : STATUS_SCHEDULED);
        campaignRepository.save(campaign);

        if (scheduleAt != null) {
            int queued = enqueueAllClientes(campaign, scheduleAt, form.getSegmento());
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

    private int enqueueAllClientes(EmailCampaign campaign, Instant scheduledAt, String segmento) {
        List<Recipient> recipients = resolveRecipients(segmento);
        int total = 0;
        for (Recipient recipient : recipients) {
            EmailCampaignQueue queue = new EmailCampaignQueue();
            queue.setCampaignId(campaign.getId());
            queue.setRecipientEmail(recipient.email());
            queue.setRecipientName(recipient.name());
            queue.setStatus(QUEUE_PENDING);
            queue.setScheduledAt(scheduledAt);
            queue.setPayloadJson(buildPayload(campaign.getNome(), segmento));
            queueRepository.save(queue);
            total++;
        }
        return total;
    }

    private List<Recipient> resolveRecipients(String segmento) {
        String seg = segmento == null ? "" : segmento.trim().toUpperCase();
        if ("VIP".equals(seg)) {
            return resolveVipRecipients();
        }
        if ("INATIVOS_90D".equals(seg)) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
            List<ClienteEntity> inativos = clienteRepository.findInativosAntesDe(cutoff);
            return toRecipients(inativos);
        }
        List<ClienteEntity> clientes = clienteRepository.findAll();
        return toRecipients(clientes);
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
            return objectMapper.writeValueAsString(Map.of(
                    "headline", campaignName == null ? "" : campaignName,
                    "message", "Confira as ofertas selecionadas para voce.",
                    "segmento", segmento == null ? "" : segmento
            ));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String buildSegmentJson(String segmento) {
        if (segmento == null || segmento.isBlank()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(Map.of("segmento", segmento));
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

    public static class CampanhaForm {
        private String nome;
        private String assunto;
        private String templateKey;
        private String segmento;
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
}
