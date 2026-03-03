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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    /**
     * Settings key for active gateway.
     */
    private static final String KEY_GATEWAY = "pg.gateway";

    /**
     * Settings key for PIX enablement.
     */
    private static final String KEY_PIX_ATIVO = "pg.pix_ativo";

    /**
     * Settings key for card enablement.
     */
    private static final String KEY_CARTAO_ATIVO = "pg.cartao_ativo";

    /**
     * Settings key for boleto enablement.
     */
    private static final String KEY_BOLETO_ATIVO = "pg.boleto_ativo";

    /**
     * Settings key for cash enablement.
     */
    private static final String KEY_DINHEIRO_ATIVO = "pg.dinheiro_ativo";

    /**
     * Settings key for card fee.
     */
    private static final String KEY_TAXA_CARTAO = "pg.taxa_cartao";

    /**
     * Settings key for max installments.
     */
    private static final String KEY_MAX_PARCELAS = "pg.max_parcelas";

    /**
     * Settings key for minimum installment amount.
     */
    private static final String KEY_PARCELA_MIN = "pg.parcela_min";

    /**
     * Settings key for gateway public key.
     */
    private static final String KEY_PUBLIC_KEY = "pg.public_key";

    /**
     * Settings key for gateway secret key.
     */
    private static final String KEY_SECRET_KEY = "pg.secret_key";

    /**
     * Settings key for gateway webhook URL.
     */
    private static final String KEY_WEBHOOK_URL = "pg.webhook_url";

    /**
     * Settings key for PIX key value.
     */
    private static final String KEY_PIX_CHAVE = "pg.pix_chave";

    /**
     * Settings key for PIX key type.
     */
    private static final String KEY_PIX_TIPO = "pg.pix_tipo";

    /**
     * Settings key for minimum cash order.
     */
    private static final String KEY_DINHEIRO_MIN = "pg.dinheiro_min";

    /**
     * Settings key for max cash change.
     */
    private static final String KEY_DINHEIRO_TROCO_MAX =
            "pg.dinheiro_troco_max";

    /**
     * Settings key for custom payment methods JSON.
     */
    private static final String KEY_CUSTOM_METHODS = "pg.custom_methods";

    /**
     * Redirect URL after any action.
     */
    private static final String REDIRECT_PAGAMENTOS =
            "redirect:/admin/configuracoes/pagamentos";

    /**
     * View path for payments settings page.
     */
    private static final String VIEW_PAGAMENTOS =
            "pages/admin/configuracoes/pagamentos";

    /**
     * Default gateway name.
     */
    private static final String DEFAULT_GATEWAY = "mercadopago";

    /**
     * Default PIX type.
     */
    private static final String DEFAULT_PIX_TIPO = "aleatoria";

    /**
     * Default max installments.
     */
    private static final int DEFAULT_MAX_PARCELAS = 6;

    /**
     * Default minimum installment amount.
     */
    private static final BigDecimal DEFAULT_PARCELA_MIN =
            BigDecimal.valueOf(20L);

    /**
     * Service used to persist settings.
     */
    private final AppSettingService settings;

    /**
     * Object mapper for custom methods payload.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates controller with dependencies.
     *
     * @param settingsService app settings service
     * @param objectMapperValue object mapper
     */
    public PagamentosConfigController(
            final AppSettingService settingsService,
            final ObjectMapper objectMapperValue
    ) {
        this.settings = settingsService;
        this.objectMapper = objectMapperValue;
    }

    /**
     * Renders payments configuration screen.
     *
     * @param model view model
     * @return settings view path
     */
    @GetMapping
    public String editar(final Model model) {
        final PagamentosForm cfg = new PagamentosForm();
        cfg.setGateway(settings.getOrDefault(KEY_GATEWAY, DEFAULT_GATEWAY));
        cfg.setPixAtivo(settings.getBoolean(KEY_PIX_ATIVO, true));
        cfg.setCartaoAtivo(settings.getBoolean(KEY_CARTAO_ATIVO, true));
        cfg.setBoletoAtivo(settings.getBoolean(KEY_BOLETO_ATIVO, false));
        cfg.setDinheiroAtivo(settings.getBoolean(KEY_DINHEIRO_ATIVO, false));
        cfg.setTaxacartao(
                settings.getDecimal(KEY_TAXA_CARTAO, BigDecimal.ZERO)
        );
        cfg.setMaxParcelas(
                settings.getInt(KEY_MAX_PARCELAS, DEFAULT_MAX_PARCELAS)
        );
        cfg.setParcelaMin(
                settings.getDecimal(KEY_PARCELA_MIN, DEFAULT_PARCELA_MIN)
        );
        cfg.setPublicKey(settings.getOrDefault(KEY_PUBLIC_KEY, ""));
        cfg.setSecretKey(settings.getOrDefault(KEY_SECRET_KEY, ""));
        cfg.setWebhookUrl(settings.getOrDefault(KEY_WEBHOOK_URL, ""));
        cfg.setPixChave(settings.getOrDefault(KEY_PIX_CHAVE, ""));
        cfg.setPixTipo(settings.getOrDefault(KEY_PIX_TIPO, DEFAULT_PIX_TIPO));
        cfg.setDinheiroMinimo(
                settings.getDecimal(KEY_DINHEIRO_MIN, BigDecimal.ZERO)
        );
        cfg.setDinheiroTrocoMax(
                settings.getDecimal(KEY_DINHEIRO_TROCO_MAX, BigDecimal.ZERO)
        );

        model.addAttribute("cfg", cfg);
        model.addAttribute("customMethods", loadCustomMethods());
        model.addAttribute("novoMetodo", new MetodoForm());
        model.addAttribute(
                "webhookConfigured",
                cfg.getWebhookUrl() != null && !cfg.getWebhookUrl().isBlank()
        );
        return VIEW_PAGAMENTOS;
    }

    /**
     * Saves main payments configuration.
     *
     * @param cfg form payload
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final PagamentosForm cfg,
            final RedirectAttributes ra
    ) {
        saveText(KEY_GATEWAY, cfg.getGateway(), "Gateway ativo");
        saveBoolean(KEY_PIX_ATIVO, cfg.getPixAtivo(), "PIX ativo");
        saveBoolean(KEY_CARTAO_ATIVO, cfg.getCartaoAtivo(), "Cartao ativo");
        saveBoolean(KEY_BOLETO_ATIVO, cfg.getBoletoAtivo(), "Boleto ativo");
        saveBoolean(
                KEY_DINHEIRO_ATIVO,
                cfg.getDinheiroAtivo(),
                "Dinheiro ativo"
        );
        saveDecimal(KEY_TAXA_CARTAO, cfg.getTaxacartao(), "Taxa de cartao");
        saveInteger(
                KEY_MAX_PARCELAS,
                cfg.getMaxParcelas(),
                "Maximo de parcelas"
        );
        saveDecimal(KEY_PARCELA_MIN, cfg.getParcelaMin(), "Parcela minima");
        saveText(KEY_PUBLIC_KEY, cfg.getPublicKey(), "Public key gateway");
        saveText(KEY_SECRET_KEY, cfg.getSecretKey(), "Secret key gateway");
        saveText(KEY_WEBHOOK_URL, cfg.getWebhookUrl(), "Webhook gateway");
        saveText(KEY_PIX_CHAVE, cfg.getPixChave(), "Chave PIX");
        saveText(KEY_PIX_TIPO, cfg.getPixTipo(), "Tipo da chave PIX");
        saveDecimal(
                KEY_DINHEIRO_MIN,
                cfg.getDinheiroMinimo(),
                "Valor minimo para dinheiro"
        );
        saveDecimal(
                KEY_DINHEIRO_TROCO_MAX,
                cfg.getDinheiroTrocoMax(),
                "Troco maximo permitido"
        );
        ra.addFlashAttribute(
                "success",
                "Configuracoes de pagamento atualizadas."
        );
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Adds one custom payment method.
     *
     * @param form method form payload
     * @param br binding result
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/metodos")
    public String adicionarMetodo(
            @ModelAttribute("novoMetodo") @Validated final MetodoForm form,
            final BindingResult br,
            final RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Preencha o nome do metodo.");
            return REDIRECT_PAGAMENTOS;
        }

        final String nome = form.getNome().trim();
        final List<MetodoPagamento> methods = loadCustomMethods();
        final boolean exists = methods.stream().anyMatch(
                method -> method.getNome().equalsIgnoreCase(nome)
        );
        if (exists) {
            ra.addFlashAttribute("error", "Metodo ja existe.");
            return REDIRECT_PAGAMENTOS;
        }

        final MetodoPagamento novo = new MetodoPagamento(
                UUID.randomUUID().toString(),
                nome,
                nullSafe(form.getTipo()),
                form.getTaxa(),
                form.getAtivo() == null || form.getAtivo()
        );
        methods.add(novo);
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo adicionado.");
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Removes one custom payment method.
     *
     * @param id method id
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/metodos/{id}/remover")
    public String removerMetodo(
            @PathVariable("id") final String id,
            final RedirectAttributes ra
    ) {
        final List<MetodoPagamento> methods = loadCustomMethods();
        methods.removeIf(method -> Objects.equals(method.getId(), id));
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo removido.");
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Reads custom methods list from JSON settings value.
     *
     * @return list of custom methods
     */
    private List<MetodoPagamento> loadCustomMethods() {
        final String raw = settings.getOrDefault(KEY_CUSTOM_METHODS, "[]");
        try {
            final List<MetodoPagamento> list = objectMapper.readValue(
                    raw,
                    new TypeReference<List<MetodoPagamento>>() {
                    }
            );
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /**
     * Persists custom methods list as JSON.
     *
     * @param methods methods list
     */
    private void saveCustomMethods(final List<MetodoPagamento> methods) {
        try {
            final String json = objectMapper.writeValueAsString(
                    methods == null ? List.of() : methods
            );
            settings.upsert(
                    KEY_CUSTOM_METHODS,
                    json,
                    "Metodos de pagamento personalizados"
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao salvar metodos de pagamento",
                    ex
            );
        }
    }

    /**
     * Saves one text setting.
     *
     * @param key setting key
     * @param value text value
     * @param description setting description
     */
    private void saveText(
            final String key,
            final String value,
            final String description
    ) {
        settings.upsert(key, nullSafe(value), description);
    }

    /**
     * Saves one boolean setting.
     *
     * @param key setting key
     * @param value boolean value
     * @param description setting description
     */
    private void saveBoolean(
            final String key,
            final Boolean value,
            final String description
    ) {
        settings.upsert(key, bool(value), description);
    }

    /**
     * Saves one integer setting.
     *
     * @param key setting key
     * @param value integer value
     * @param description setting description
     */
    private void saveInteger(
            final String key,
            final Integer value,
            final String description
    ) {
        settings.upsert(key, intVal(value), description);
    }

    /**
     * Saves one decimal setting.
     *
     * @param key setting key
     * @param value decimal value
     * @param description setting description
     */
    private void saveDecimal(
            final String key,
            final BigDecimal value,
            final String description
    ) {
        settings.upsert(key, decimal(value), description);
    }

    /**
     * Converts boolean to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String bool(final Boolean value) {
        return value != null && value ? "true" : "false";
    }

    /**
     * Converts integer to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String intVal(final Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Converts decimal to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String decimal(final BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    /**
     * Returns trimmed value or empty string.
     *
     * @param value source value
     * @return null-safe string
     */
    private String nullSafe(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Form payload for main payment settings.
     */
    @Getter
    @Setter
    public static final class PagamentosForm {

        /**
         * PIX enablement flag.
         */
        private Boolean pixAtivo;

        /**
         * Card enablement flag.
         */
        private Boolean cartaoAtivo;

        /**
         * Boleto enablement flag.
         */
        private Boolean boletoAtivo;

        /**
         * Cash enablement flag.
         */
        private Boolean dinheiroAtivo;

        /**
         * Active gateway.
         */
        private String gateway;

        /**
         * Card fee percentage.
         */
        private BigDecimal taxacartao;

        /**
         * Maximum installments.
         */
        private Integer maxParcelas;

        /**
         * Minimum installment amount.
         */
        private BigDecimal parcelaMin;

        /**
         * Gateway public key.
         */
        private String publicKey;

        /**
         * Gateway secret key.
         */
        private String secretKey;

        /**
         * Gateway webhook URL.
         */
        private String webhookUrl;

        /**
         * PIX key value.
         */
        private String pixChave;

        /**
         * PIX key type.
         */
        private String pixTipo;

        /**
         * Minimum cash amount.
         */
        private BigDecimal dinheiroMinimo;

        /**
         * Maximum cash change amount.
         */
        private BigDecimal dinheiroTrocoMax;
    }

    /**
     * Form payload for custom method creation.
     */
    @Getter
    @Setter
    public static final class MetodoForm {

        /**
         * Method display name.
         */
        @NotBlank(message = "Nome do metodo obrigatorio")
        private String nome;

        /**
         * Method type.
         */
        private String tipo;

        /**
         * Method fee.
         */
        private BigDecimal taxa;

        /**
         * Method active flag.
         */
        private Boolean ativo;
    }

    /**
     * Persisted custom payment method.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class MetodoPagamento {

        /**
         * Method identifier.
         */
        private String id;

        /**
         * Method name.
         */
        private String nome;

        /**
         * Method type.
         */
        private String tipo;

        /**
         * Method fee.
         */
        private BigDecimal taxa;

        /**
         * Method active flag.
         */
        private boolean ativo;

        /**
         * Creates a full method payload.
         *
         * @param idValue identifier
         * @param nomeValue name
         * @param tipoValue type
         * @param taxaValue fee
         * @param ativoValue active flag
         */
        public MetodoPagamento(
                final String idValue,
                final String nomeValue,
                final String tipoValue,
                final BigDecimal taxaValue,
                final boolean ativoValue
        ) {
            this.id = idValue;
            this.nome = nomeValue;
            this.tipo = tipoValue;
            this.taxa = taxaValue;
            this.ativo = ativoValue;
        }
    }
}
