package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/pagamentos")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class PagamentosConfigController {

    private static final String KEY_GATEWAY = "pg.gateway";
    private static final String KEY_PIX_ATIVO = "pg.pix_ativo";
    private static final String KEY_CARTAO_ATIVO = "pg.cartao_ativo";
    private static final String KEY_BOLETO_ATIVO = "pg.boleto_ativo";
    private static final String KEY_DINHEIRO_ATIVO = "pg.dinheiro_ativo";
    private static final String KEY_TAXA_CARTAO = "pg.taxa_cartao";
    private static final String KEY_MAX_PARCELAS = "pg.max_parcelas";
    private static final String KEY_PARCELA_MIN = "pg.parcela_min";
    private static final String KEY_PUBLIC_KEY = "pg.public_key";
    private static final String KEY_SECRET_KEY = "pg.secret_key";
    private static final String KEY_WEBHOOK_URL = "pg.webhook_url";
    private static final String KEY_PIX_CHAVE = "pg.pix_chave";
    private static final String KEY_PIX_TIPO = "pg.pix_tipo";
    private static final String KEY_DINHEIRO_MIN = "pg.dinheiro_min";
    private static final String KEY_DINHEIRO_TROCO_MAX = "pg.dinheiro_troco_max";
    private static final String KEY_CUSTOM_METHODS = "pg.custom_methods";

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public PagamentosConfigController(AppSettingService settings, ObjectMapper objectMapper) {
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String editar(Model model) {
        PagamentosForm cfg = new PagamentosForm();
        cfg.setGateway(settings.getOrDefault(KEY_GATEWAY, "mercadopago"));
        cfg.setPixAtivo(settings.getBoolean(KEY_PIX_ATIVO, true));
        cfg.setCartaoAtivo(settings.getBoolean(KEY_CARTAO_ATIVO, true));
        cfg.setBoletoAtivo(settings.getBoolean(KEY_BOLETO_ATIVO, false));
        cfg.setDinheiroAtivo(settings.getBoolean(KEY_DINHEIRO_ATIVO, false));
        cfg.setTaxacartao(settings.getDecimal(KEY_TAXA_CARTAO, BigDecimal.ZERO));
        cfg.setMaxParcelas(settings.getInt(KEY_MAX_PARCELAS, 6));
        cfg.setParcelaMin(settings.getDecimal(KEY_PARCELA_MIN, BigDecimal.valueOf(20)));
        cfg.setPublicKey(settings.getOrDefault(KEY_PUBLIC_KEY, ""));
        cfg.setSecretKey(settings.getOrDefault(KEY_SECRET_KEY, ""));
        cfg.setWebhookUrl(settings.getOrDefault(KEY_WEBHOOK_URL, ""));
        cfg.setPixChave(settings.getOrDefault(KEY_PIX_CHAVE, ""));
        cfg.setPixTipo(settings.getOrDefault(KEY_PIX_TIPO, "aleatoria"));
        cfg.setDinheiroMinimo(settings.getDecimal(KEY_DINHEIRO_MIN, BigDecimal.ZERO));
        cfg.setDinheiroTrocoMax(settings.getDecimal(KEY_DINHEIRO_TROCO_MAX, BigDecimal.ZERO));

        model.addAttribute("cfg", cfg);
        model.addAttribute("customMethods", loadCustomMethods());
        model.addAttribute("novoMetodo", new MetodoForm());
        model.addAttribute("webhookConfigured", cfg.getWebhookUrl() != null && !cfg.getWebhookUrl().isBlank());
        return "pages/admin/configuracoes/pagamentos";
    }

    @PostMapping
    public String salvar(@ModelAttribute("cfg") PagamentosForm cfg, RedirectAttributes ra) {
        settings.upsert(KEY_GATEWAY, nullSafe(cfg.getGateway()), "Gateway ativo");
        settings.upsert(KEY_PIX_ATIVO, bool(cfg.getPixAtivo()), "PIX ativo");
        settings.upsert(KEY_CARTAO_ATIVO, bool(cfg.getCartaoAtivo()), "Cartao ativo");
        settings.upsert(KEY_BOLETO_ATIVO, bool(cfg.getBoletoAtivo()), "Boleto ativo");
        settings.upsert(KEY_DINHEIRO_ATIVO, bool(cfg.getDinheiroAtivo()), "Dinheiro ativo");
        settings.upsert(KEY_TAXA_CARTAO, decimal(cfg.getTaxacartao()), "Taxa de cartao");
        settings.upsert(KEY_MAX_PARCELAS, intVal(cfg.getMaxParcelas()), "Maximo de parcelas");
        settings.upsert(KEY_PARCELA_MIN, decimal(cfg.getParcelaMin()), "Parcela minima");
        settings.upsert(KEY_PUBLIC_KEY, nullSafe(cfg.getPublicKey()), "Public key gateway");
        settings.upsert(KEY_SECRET_KEY, nullSafe(cfg.getSecretKey()), "Secret key gateway");
        settings.upsert(KEY_WEBHOOK_URL, nullSafe(cfg.getWebhookUrl()), "Webhook gateway");
        settings.upsert(KEY_PIX_CHAVE, nullSafe(cfg.getPixChave()), "Chave PIX");
        settings.upsert(KEY_PIX_TIPO, nullSafe(cfg.getPixTipo()), "Tipo da chave PIX");
        settings.upsert(KEY_DINHEIRO_MIN, decimal(cfg.getDinheiroMinimo()), "Valor minimo para dinheiro");
        settings.upsert(KEY_DINHEIRO_TROCO_MAX, decimal(cfg.getDinheiroTrocoMax()), "Troco maximo permitido");
        ra.addFlashAttribute("success", "Configuracoes de pagamento atualizadas.");
        return "redirect:/admin/configuracoes/pagamentos";
    }

    @PostMapping("/metodos")
    public String adicionarMetodo(@ModelAttribute("novoMetodo") @Validated MetodoForm form, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Preencha o nome do metodo.");
            return "redirect:/admin/configuracoes/pagamentos";
        }
        String nome = form.getNome().trim();
        List<MetodoPagamento> methods = loadCustomMethods();
        boolean exists = methods.stream().anyMatch(m -> m.getNome().equalsIgnoreCase(nome));
        if (exists) {
            ra.addFlashAttribute("error", "Metodo ja existe.");
            return "redirect:/admin/configuracoes/pagamentos";
        }
        MetodoPagamento novo = new MetodoPagamento(
                UUID.randomUUID().toString(),
                nome,
                form.getTipo(),
                form.getTaxa(),
                form.getAtivo() == null || form.getAtivo()
        );
        methods.add(novo);
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo adicionado.");
        return "redirect:/admin/configuracoes/pagamentos";
    }

    @PostMapping("/metodos/{id}/remover")
    public String removerMetodo(@PathVariable String id, RedirectAttributes ra) {
        List<MetodoPagamento> methods = loadCustomMethods();
        methods.removeIf(m -> Objects.equals(m.getId(), id));
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo removido.");
        return "redirect:/admin/configuracoes/pagamentos";
    }

    private List<MetodoPagamento> loadCustomMethods() {
        String raw = settings.getOrDefault(KEY_CUSTOM_METHODS, "[]");
        try {
            List<MetodoPagamento> list = objectMapper.readValue(raw, new TypeReference<List<MetodoPagamento>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private void saveCustomMethods(List<MetodoPagamento> methods) {
        try {
            String json = objectMapper.writeValueAsString(methods == null ? List.of() : methods);
            settings.upsert(KEY_CUSTOM_METHODS, json, "Metodos de pagamento personalizados");
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao salvar metodos de pagamento", ex);
        }
    }

    private String bool(Boolean v) {
        return v != null && v ? "true" : "false";
    }

    private String intVal(Integer v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String decimal(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }

    private String nullSafe(String v) {
        return v == null ? "" : v.trim();
    }

    public static class PagamentosForm {
        private Boolean pixAtivo;
        private Boolean cartaoAtivo;
        private Boolean boletoAtivo;
        private Boolean dinheiroAtivo;
        private String gateway;
        private BigDecimal taxacartao;
        private Integer maxParcelas;
        private BigDecimal parcelaMin;
        private String publicKey;
        private String secretKey;
        private String webhookUrl;
        private String pixChave;
        private String pixTipo;
        private BigDecimal dinheiroMinimo;
        private BigDecimal dinheiroTrocoMax;

        public Boolean getPixAtivo() {
            return pixAtivo;
        }

        public void setPixAtivo(Boolean pixAtivo) {
            this.pixAtivo = pixAtivo;
        }

        public Boolean getCartaoAtivo() {
            return cartaoAtivo;
        }

        public void setCartaoAtivo(Boolean cartaoAtivo) {
            this.cartaoAtivo = cartaoAtivo;
        }

        public Boolean getBoletoAtivo() {
            return boletoAtivo;
        }

        public void setBoletoAtivo(Boolean boletoAtivo) {
            this.boletoAtivo = boletoAtivo;
        }

        public Boolean getDinheiroAtivo() {
            return dinheiroAtivo;
        }

        public void setDinheiroAtivo(Boolean dinheiroAtivo) {
            this.dinheiroAtivo = dinheiroAtivo;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public BigDecimal getTaxacartao() {
            return taxacartao;
        }

        public void setTaxacartao(BigDecimal taxacartao) {
            this.taxacartao = taxacartao;
        }

        public Integer getMaxParcelas() {
            return maxParcelas;
        }

        public void setMaxParcelas(Integer maxParcelas) {
            this.maxParcelas = maxParcelas;
        }

        public BigDecimal getParcelaMin() {
            return parcelaMin;
        }

        public void setParcelaMin(BigDecimal parcelaMin) {
            this.parcelaMin = parcelaMin;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public String getPixChave() {
            return pixChave;
        }

        public void setPixChave(String pixChave) {
            this.pixChave = pixChave;
        }

        public String getPixTipo() {
            return pixTipo;
        }

        public void setPixTipo(String pixTipo) {
            this.pixTipo = pixTipo;
        }

        public BigDecimal getDinheiroMinimo() {
            return dinheiroMinimo;
        }

        public void setDinheiroMinimo(BigDecimal dinheiroMinimo) {
            this.dinheiroMinimo = dinheiroMinimo;
        }

        public BigDecimal getDinheiroTrocoMax() {
            return dinheiroTrocoMax;
        }

        public void setDinheiroTrocoMax(BigDecimal dinheiroTrocoMax) {
            this.dinheiroTrocoMax = dinheiroTrocoMax;
        }
    }

    public static class MetodoForm {
        @NotBlank(message = "Nome do metodo obrigatorio")
        private String nome;
        private String tipo;
        private BigDecimal taxa;
        private Boolean ativo;

        public String getNome() {
            return nome == null ? "" : nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo == null ? "" : tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getTaxa() {
            return taxa;
        }

        public void setTaxa(BigDecimal taxa) {
            this.taxa = taxa;
        }

        public Boolean getAtivo() {
            return ativo;
        }

        public void setAtivo(Boolean ativo) {
            this.ativo = ativo;
        }
    }

    public static class MetodoPagamento {
        private String id;
        private String nome;
        private String tipo;
        private BigDecimal taxa;
        private boolean ativo;

        public MetodoPagamento() {
        }

        public MetodoPagamento(String id, String nome, String tipo, BigDecimal taxa, boolean ativo) {
            this.id = id;
            this.nome = nome;
            this.tipo = tipo;
            this.taxa = taxa;
            this.ativo = ativo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public BigDecimal getTaxa() {
            return taxa;
        }

        public void setTaxa(BigDecimal taxa) {
            this.taxa = taxa;
        }

        public boolean isAtivo() {
            return ativo;
        }

        public void setAtivo(boolean ativo) {
            this.ativo = ativo;
        }
    }
}
