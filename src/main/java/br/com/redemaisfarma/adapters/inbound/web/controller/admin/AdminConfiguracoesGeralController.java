package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/geral")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesGeralController {

    /**
     * Logger used by this controller.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminConfiguracoesGeralController.class);

    /**
     * Redirect URL after save.
     */
    private static final String REDIRECT_URL =
            "redirect:/admin/configuracoes/geral";

    /**
     * View path for form page.
     */
    private static final String VIEW_PATH = "pages/admin/configuracoes/geral";

    /**
     * Flash attribute key for warning messages.
     */
    private static final String WARNING_ATTR = "warning";

    /**
     * Flash attribute key for success messages.
     */
    private static final String SUCCESS_ATTR = "success";

    /**
     * Section id for brand and identity.
     */
    private static final String SECTION_IDENTIDADE = "identidade";

    /**
     * Section id for contact settings.
     */
    private static final String SECTION_CONTATO = "contato";

    /**
     * Section id for address settings.
     */
    private static final String SECTION_ENDERECO = "endereco";

    /**
     * Section id for delivery settings.
     */
    private static final String SECTION_ENTREGA = "entrega";

    /**
     * Section id for preferences settings.
     */
    private static final String SECTION_PREFERENCIAS = "preferencias";

    /**
     * Section id for stock alert settings.
     */
    private static final String SECTION_ALERTAS = "alertas";

    /**
     * Upload folder root segment.
     */
    private static final String UPLOAD_DIR_MEDIA = "media";

    /**
     * Upload folder branding segment.
     */
    private static final String UPLOAD_DIR_BRANDING = "branding";

    /**
     * Default stock alert threshold.
     */
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 10;

    /**
     * Default stock alert cooldown in minutes.
     */
    private static final int DEFAULT_ALERTA_ESTOQUE_COOLDOWN = 60;

    /**
     * Default cron for stock alert scheduler.
     */
    private static final String DEFAULT_ALERTA_ESTOQUE_CRON =
            "0 */30 * * * *";

    /**
     * Mapping between current keys and legacy keys.
     */
    private static final Map<String, String> KEY_ALIASES = Map.ofEntries(
            Map.entry("loja.nome", "GERAL.nome_sistema"),
            Map.entry("loja.nome_fantasia", "GERAL.nome_fantasia"),
            Map.entry("loja.razao_social", "GERAL.razao_social"),
            Map.entry("loja.nome_exibicao", "GERAL.nome_loja_site"),
            Map.entry("loja.slogan", "GERAL.slogan_loja"),
            Map.entry("branding.logo_url", "GERAL.logo_inicial_url"),
            Map.entry("branding.favicon_url", "GERAL.favicon_url"),
            Map.entry(
                    "branding.home_hero_url",
                    "GERAL.home_hero_imagem_url"
            ),
            Map.entry("branding.home_hero_texto", "GERAL.home_hero_texto"),
            Map.entry("contato.email", "GERAL.email"),
            Map.entry("contato.telefone", "GERAL.telefone"),
            Map.entry("contato.whatsapp", "GERAL.whatsapp"),
            Map.entry("contato.instagram", "GERAL.instagram"),
            Map.entry("contato.site_url", "GERAL.site_url"),
            Map.entry("endereco.logradouro", "GERAL.endereco"),
            Map.entry("endereco.cidade", "GERAL.cidade"),
            Map.entry("endereco.estado", "GERAL.estado"),
            Map.entry("endereco.cep", "GERAL.cep"),
            Map.entry("endereco.bairro", "GERAL.bairro"),
            Map.entry(
                    "entrega.horario_atendimento",
                    "GERAL.horario_atendimento"
            ),
            Map.entry("entrega.taxa", "GERAL.taxa_entrega"),
            Map.entry("entrega.pedido_minimo", "GERAL.pedido_minimo"),
            Map.entry(
                    "preferencias.cadastro_rapido",
                    "GERAL.habilitar_cadastro_rapido"
            ),
            Map.entry(
                    "preferencias.assinaturas",
                    "GERAL.habilitar_assinaturas"
            ),
            Map.entry(
                    "preferencias.notificacoes",
                    "GERAL.habilitar_notificacoes"
            ),
            Map.entry(
                    "preferencias.exibir_destaques_home",
                    "GERAL.exibir_destaques_home"
            )
    );

    /**
     * Keys that do not have legacy aliases.
     */
    private static final Set<String> DIRECT_KEYS = Set.of(
            "branding.home_hero_video_url",
            "retirada.ativa",
            "retirada.horario",
            "retirada.instrucoes",
            "app.estoque.alerta.enabled",
            "app.estoque.alerta.limite",
            "app.estoque.alerta.cooldown-minutes",
            "app.estoque.alerta.cron"
    );

    /**
     * All keys fetched in one read operation.
     */
    private static final Set<String> ALL_KEYS = buildAllKeys();

    /**
     * Service used to read and save settings.
     */
    private final AppSettingService settings;

    /**
     * Creates controller with settings service.
     *
     * @param appSettingService settings service
     */
    public AdminConfiguracoesGeralController(
            final AppSettingService appSettingService
    ) {
        this.settings = appSettingService;
    }

    /**
     * Renders the general configuration form.
     *
     * @param model view model
     * @return form view path
     */
    @GetMapping
    public String form(final Model model) {
        model.addAttribute("cfg", loadForm());
        return VIEW_PATH;
    }

    /**
     * Persists general configuration settings.
     *
     * @param cfg form payload
     * @param logoInicialFile optional logo image
     * @param homeHeroImagem optional hero image
     * @param section optional section selector
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final ConfigGeralForm cfg,
            @RequestParam(name = "logoInicialFile", required = false)
            final MultipartFile logoInicialFile,
            @RequestParam(name = "homeHeroImagem", required = false)
            final MultipartFile homeHeroImagem,
            @RequestParam(name = "section", required = false)
            final String section,
            final RedirectAttributes ra
    ) {
        final String normalizedSection = normalizeSection(section);
        final boolean saveAll = normalizedSection == null;
        boolean uploadFailed = false;
        final List<String> uploadErrors = new ArrayList<>();

        if (shouldSaveSection(saveAll, normalizedSection, SECTION_IDENTIDADE)) {
            uploadFailed = processUpload(
                    logoInicialFile,
                    "logo",
                    cfg::setLogoInicialUrl,
                    "Falha ao salvar logo inicial.",
                    uploadErrors
            ) || uploadFailed;
            uploadFailed = processUpload(
                    homeHeroImagem,
                    "home-hero",
                    cfg::setHomeHeroImagemUrl,
                    "Falha ao salvar imagem do hero.",
                    uploadErrors
            ) || uploadFailed;
            persistIdentidadeSettings(cfg);
        }

        if (shouldSaveSection(saveAll, normalizedSection, SECTION_CONTATO)) {
            persistContatoSettings(cfg);
        }

        if (shouldSaveSection(saveAll, normalizedSection, SECTION_ENDERECO)) {
            persistEnderecoSettings(cfg);
        }

        if (shouldSaveSection(saveAll, normalizedSection, SECTION_ENTREGA)) {
            persistEntregaSettings(cfg);
        }

        if (shouldSaveSection(
                saveAll,
                normalizedSection,
                SECTION_PREFERENCIAS
        )) {
            persistPreferenciasSettings(cfg);
        }

        if (shouldSaveSection(saveAll, normalizedSection, SECTION_ALERTAS)) {
            persistAlertasSettings(cfg);
        }

        if (uploadFailed) {
            final String warningMessage = uploadErrors.isEmpty()
                    ? "Alguns uploads falharam; verifique os arquivos enviados."
                    : String.join(" ", uploadErrors);
            ra.addFlashAttribute(WARNING_ATTR, warningMessage);
        } else {
            ra.addFlashAttribute(
                    SUCCESS_ATTR,
                    "Configuracoes gerais atualizadas."
            );
        }

        return REDIRECT_URL;
    }

    /**
     * Normalizes section string when present.
     *
     * @param section raw section
     * @return normalized section or null when all sections must be saved
     */
    private String normalizeSection(final String section) {
        if (section == null || section.isBlank()) {
            return null;
        }
        return section;
    }

    /**
     * Evaluates if one section should be persisted.
     *
     * @param saveAll true when all sections should be saved
     * @param section requested section
     * @param target target section
     * @return true when section must be persisted
     */
    private boolean shouldSaveSection(
            final boolean saveAll,
            final String section,
            final String target
    ) {
        return saveAll || target.equals(section);
    }

    /**
     * Persists identity and branding settings.
     *
     * @param cfg form payload
     */
    private void persistIdentidadeSettings(final ConfigGeralForm cfg) {
        saveTextSetting("loja.nome", cfg.getNomeSistema(), "Nome do sistema");
        saveTextSetting(
                "loja.nome_fantasia",
                cfg.getNomeFantasia(),
                "Nome fantasia"
        );
        saveTextSetting(
                "loja.razao_social",
                cfg.getRazaoSocial(),
                "Razao social"
        );
        saveTextSetting(
                "loja.nome_exibicao",
                cfg.getNomeLojaSite(),
                "Nome exibido no site"
        );
        saveTextSetting("loja.slogan", cfg.getSloganLoja(), "Slogan");
        saveTextSetting(
                "branding.logo_url",
                cfg.getLogoInicialUrl(),
                "Logo inicial"
        );
        saveTextSetting(
                "branding.favicon_url",
                cfg.getFaviconUrl(),
                "Favicon"
        );
        saveTextSetting(
                "branding.home_hero_url",
                cfg.getHomeHeroImagemUrl(),
                "Imagem principal da home"
        );
        saveTextSetting(
                "branding.home_hero_texto",
                cfg.getHomeHeroTexto(),
                "Texto do destaque"
        );
        saveTextSetting(
                "branding.home_hero_video_url",
                cfg.getHomeHeroVideoUrl(),
                "Video principal da home"
        );
    }

    /**
     * Persists contact settings.
     *
     * @param cfg form payload
     */
    private void persistContatoSettings(final ConfigGeralForm cfg) {
        saveTextSetting("contato.email", cfg.getEmail(), "Email principal");
        saveTextSetting(
                "contato.telefone",
                cfg.getTelefone(),
                "Telefone principal"
        );
        saveTextSetting("contato.whatsapp", cfg.getWhatsapp(), "Whatsapp");
        saveTextSetting("contato.instagram", cfg.getInstagram(), "Instagram");
        saveTextSetting("contato.site_url", cfg.getSiteUrl(), "Site oficial");
    }

    /**
     * Persists address settings.
     *
     * @param cfg form payload
     */
    private void persistEnderecoSettings(final ConfigGeralForm cfg) {
        saveTextSetting(
                "endereco.logradouro",
                cfg.getEndereco(),
                "Endereco"
        );
        saveTextSetting("endereco.cidade", cfg.getCidade(), "Cidade");
        saveTextSetting("endereco.estado", cfg.getEstado(), "Estado");
        saveTextSetting("endereco.cep", cfg.getCep(), "CEP");
        saveTextSetting("endereco.bairro", cfg.getBairro(), "Bairro");
    }

    /**
     * Persists delivery settings.
     *
     * @param cfg form payload
     */
    private void persistEntregaSettings(final ConfigGeralForm cfg) {
        saveTextSetting(
                "entrega.horario_atendimento",
                cfg.getHorarioAtendimento(),
                "Horario de atendimento"
        );
        saveTextSetting(
                "entrega.taxa",
                cfg.getTaxaEntrega(),
                "Taxa de entrega"
        );
        saveTextSetting(
                "entrega.pedido_minimo",
                cfg.getPedidoMinimo(),
                "Pedido minimo"
        );
        saveBooleanSetting(
                "retirada.ativa",
                cfg.getRetiradaAtiva(),
                "Retirada na loja ativa"
        );
        saveTextSetting(
                "retirada.horario",
                cfg.getRetiradaHorario(),
                "Horario de retirada"
        );
        saveTextSetting(
                "retirada.instrucoes",
                cfg.getRetiradaInstrucoes(),
                "Instrucoes de retirada"
        );
    }

    /**
     * Persists preferences settings.
     *
     * @param cfg form payload
     */
    private void persistPreferenciasSettings(final ConfigGeralForm cfg) {
        saveBooleanSetting(
                "preferencias.cadastro_rapido",
                cfg.getHabilitarCadastrorapido(),
                "Cadastro rapido"
        );
        saveBooleanSetting(
                "preferencias.assinaturas",
                cfg.getHabilitarAssinaturas(),
                "Assinaturas"
        );
        saveBooleanSetting(
                "preferencias.notificacoes",
                cfg.getHabilitarNotificacoes(),
                "Notificacoes"
        );
        saveBooleanSetting(
                "preferencias.exibir_destaques_home",
                cfg.getExibirDestaquesHome(),
                "Destaques na home"
        );
    }

    /**
     * Persists stock alert settings.
     *
     * @param cfg form payload
     */
    private void persistAlertasSettings(final ConfigGeralForm cfg) {
        saveBooleanSetting(
                "app.estoque.alerta.enabled",
                cfg.getAlertaEstoqueAtivo(),
                "Alerta estoque ativo"
        );
        saveTextSetting(
                "app.estoque.alerta.limite",
                cfg.getAlertaEstoqueLimite(),
                "Alerta estoque limite"
        );
        saveTextSetting(
                "app.estoque.alerta.cooldown-minutes",
                cfg.getAlertaEstoqueCooldown(),
                "Alerta estoque cooldown (min)"
        );
        saveTextSetting(
                "app.estoque.alerta.cron",
                cfg.getAlertaEstoqueCron(),
                "Alerta estoque cron"
        );
    }

    /**
     * Persists one text setting.
     *
     * @param key setting key
     * @param value setting value
     * @param description setting description
     */
    private void saveTextSetting(
            final String key,
            final String value,
            final String description
    ) {
        settings.upsert(key, nullSafe(value), description);
    }

    /**
     * Persists one boolean setting.
     *
     * @param key setting key
     * @param value setting value
     * @param description setting description
     */
    private void saveBooleanSetting(
            final String key,
            final Boolean value,
            final String description
    ) {
        settings.upsert(key, bool(value), description);
    }

    /**
     * Loads all form values from persisted settings.
     *
     * @return populated form
     */
    private ConfigGeralForm loadForm() {
        final Map<String, String> cfg = settings.getAllByKeys(ALL_KEYS);
        final ConfigGeralForm form = new ConfigGeralForm();

        form.setNomeSistema(readStringWithFallback(cfg, "loja.nome"));
        form.setNomeFantasia(readStringWithFallback(cfg, "loja.nome_fantasia"));
        form.setRazaoSocial(readStringWithFallback(cfg, "loja.razao_social"));
        form.setNomeLojaSite(readStringWithFallback(cfg, "loja.nome_exibicao"));
        form.setSloganLoja(readStringWithFallback(cfg, "loja.slogan"));
        form.setLogoInicialUrl(
                readStringWithFallback(cfg, "branding.logo_url")
        );
        form.setFaviconUrl(readStringWithFallback(cfg, "branding.favicon_url"));
        form.setHomeHeroImagemUrl(
                readStringWithFallback(cfg, "branding.home_hero_url")
        );
        form.setHomeHeroTexto(
                readStringWithFallback(cfg, "branding.home_hero_texto")
        );
        form.setHomeHeroVideoUrl(
                readString(cfg, "branding.home_hero_video_url")
        );

        form.setEmail(readStringWithFallback(cfg, "contato.email"));
        form.setTelefone(readStringWithFallback(cfg, "contato.telefone"));
        form.setWhatsapp(readStringWithFallback(cfg, "contato.whatsapp"));
        form.setInstagram(readStringWithFallback(cfg, "contato.instagram"));
        form.setSiteUrl(readStringWithFallback(cfg, "contato.site_url"));

        form.setEndereco(readStringWithFallback(cfg, "endereco.logradouro"));
        form.setCidade(readStringWithFallback(cfg, "endereco.cidade"));
        form.setEstado(readStringWithFallback(cfg, "endereco.estado"));
        form.setCep(readStringWithFallback(cfg, "endereco.cep"));
        form.setBairro(readStringWithFallback(cfg, "endereco.bairro"));

        form.setHorarioAtendimento(
                readStringWithFallback(cfg, "entrega.horario_atendimento")
        );
        form.setTaxaEntrega(readStringWithFallback(cfg, "entrega.taxa"));
        form.setPedidoMinimo(
                readStringWithFallback(cfg, "entrega.pedido_minimo")
        );

        form.setRetiradaAtiva(readBoolean(cfg, "retirada.ativa", false));
        form.setRetiradaHorario(readString(cfg, "retirada.horario"));
        form.setRetiradaInstrucoes(readString(cfg, "retirada.instrucoes"));

        form.setHabilitarCadastrorapido(
                readBooleanWithFallback(cfg, "preferencias.cadastro_rapido")
        );
        form.setHabilitarAssinaturas(
                readBooleanWithFallback(cfg, "preferencias.assinaturas")
        );
        form.setHabilitarNotificacoes(
                readBooleanWithFallback(cfg, "preferencias.notificacoes")
        );
        form.setExibirDestaquesHome(
                readBooleanWithFallback(
                        cfg,
                        "preferencias.exibir_destaques_home"
                )
        );

        form.setAlertaEstoqueAtivo(
                readBoolean(cfg, "app.estoque.alerta.enabled", true)
        );
        form.setAlertaEstoqueLimite(String.valueOf(readInt(
                cfg,
                "app.estoque.alerta.limite",
                DEFAULT_ALERTA_ESTOQUE_LIMITE
        )));
        form.setAlertaEstoqueCooldown(String.valueOf(readInt(
                cfg,
                "app.estoque.alerta.cooldown-minutes",
                DEFAULT_ALERTA_ESTOQUE_COOLDOWN
        )));
        form.setAlertaEstoqueCron(readString(
                cfg,
                "app.estoque.alerta.cron",
                DEFAULT_ALERTA_ESTOQUE_CRON
        ));

        return form;
    }

    /**
     * Reads text value using canonical key and legacy fallback.
     *
     * @param cfg settings map
     * @param primaryKey canonical key
     * @return resolved value
     */
    private String readStringWithFallback(
            final Map<String, String> cfg,
            final String primaryKey
    ) {
        final String value = cfg.get(primaryKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        final String legacyKey = KEY_ALIASES.get(primaryKey);
        if (legacyKey == null) {
            return "";
        }

        return cfg.getOrDefault(legacyKey, "");
    }

    /**
     * Reads text value by key, defaulting to empty string.
     *
     * @param cfg settings map
     * @param key setting key
     * @return resolved value
     */
    private String readString(
            final Map<String, String> cfg,
            final String key
    ) {
        return readString(cfg, key, "");
    }

    /**
     * Reads text value by key with explicit default.
     *
     * @param cfg settings map
     * @param key setting key
     * @param defaultValue default value
     * @return resolved value
     */
    private String readString(
            final Map<String, String> cfg,
            final String key,
            final String defaultValue
    ) {
        return cfg.getOrDefault(key, defaultValue);
    }

    /**
     * Reads boolean using canonical key and legacy fallback.
     *
     * @param cfg settings map
     * @param primaryKey canonical key
     * @return resolved boolean value
     */
    private boolean readBooleanWithFallback(
            final Map<String, String> cfg,
            final String primaryKey
    ) {
        if (cfg.containsKey(primaryKey)) {
            return parseBoolean(cfg.get(primaryKey), false);
        }

        final String legacyKey = KEY_ALIASES.get(primaryKey);
        if (legacyKey == null) {
            return false;
        }

        return parseBoolean(cfg.get(legacyKey), false);
    }

    /**
     * Reads boolean by key with default.
     *
     * @param cfg settings map
     * @param key setting key
     * @param defaultValue default value
     * @return resolved boolean value
     */
    private boolean readBoolean(
            final Map<String, String> cfg,
            final String key,
            final boolean defaultValue
    ) {
        return parseBoolean(cfg.get(key), defaultValue);
    }

    /**
     * Parses boolean values in app format.
     *
     * @param value source value
     * @param defaultValue default value for null or blank
     * @return parsed boolean value
     */
    private boolean parseBoolean(
            final String value,
            final boolean defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        final String normalized = value.trim().toLowerCase();
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "on".equals(normalized);
    }

    /**
     * Reads integer by key with default.
     *
     * @param cfg settings map
     * @param key setting key
     * @param defaultValue default value
     * @return resolved integer value
     */
    private int readInt(
            final Map<String, String> cfg,
            final String key,
            final int defaultValue
    ) {
        final String value = cfg.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Converts null to empty string.
     *
     * @param value source value
     * @return null-safe value
     */
    private static String nullSafe(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Converts boolean to string expected by settings table.
     *
     * @param value source value
     * @return boolean string representation
     */
    private static String bool(final Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    /**
     * Processes one optional upload and applies resulting URL.
     *
     * @param file uploaded file
     * @param prefix filename prefix
     * @param onSuccess callback to receive resulting URL
     * @param errorMessage error message for failures
     * @param uploadErrors collected upload errors
     * @return true when upload failed
     */
    private boolean processUpload(
            final MultipartFile file,
            final String prefix,
            final Consumer<String> onSuccess,
            final String errorMessage,
            final List<String> uploadErrors
    ) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try {
            onSuccess.accept(storeUpload(file, prefix));
            return false;
        } catch (IOException ex) {
            appendUploadError(uploadErrors, errorMessage, ex);
            return true;
        }
    }

    /**
     * Adds one upload error to list and logs warning.
     *
     * @param uploadErrors collected upload errors
     * @param errorMessage error message
     * @param ex root cause
     */
    private void appendUploadError(
            final List<String> uploadErrors,
            final String errorMessage,
            final IOException ex
    ) {
        uploadErrors.add(errorMessage);
        LOGGER.warn(errorMessage, ex);
    }

    /**
     * Stores a branding file and returns public URL.
     *
     * @param file uploaded file
     * @param prefix filename prefix
     * @return public URL path
     * @throws IOException when write operation fails
     */
    private static String storeUpload(
            final MultipartFile file,
            final String prefix
    ) throws IOException {
        final String original = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "upload")
        );
        String ext = "";
        final int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }

        final String filename = prefix + "-" + System.currentTimeMillis() + ext;
        final Path dir = Paths.get(UPLOAD_DIR_MEDIA, UPLOAD_DIR_BRANDING);
        Files.createDirectories(dir);

        final Path target = dir.resolve(filename);
        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
        return "/media/branding/" + filename;
    }

    /**
     * Builds the full set of keys used by this controller.
     *
     * @return immutable set containing all keys
     */
    private static Set<String> buildAllKeys() {
        final Set<String> keys = new LinkedHashSet<>(DIRECT_KEYS);
        for (Map.Entry<String, String> entry : KEY_ALIASES.entrySet()) {
            keys.add(entry.getKey());
            keys.add(entry.getValue());
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * Form object used by general settings page.
     */
    @Getter
    @Setter
    public static final class ConfigGeralForm {

        /**
         * System name.
         */
        private String nomeSistema;

        /**
         * Store trade name.
         */
        private String nomeFantasia;

        /**
         * Company legal name.
         */
        private String razaoSocial;

        /**
         * Public store name shown on site.
         */
        private String nomeLojaSite;

        /**
         * Store slogan.
         */
        private String sloganLoja;

        /**
         * Initial logo URL.
         */
        private String logoInicialUrl;

        /**
         * Favicon URL.
         */
        private String faviconUrl;

        /**
         * Home hero image URL.
         */
        private String homeHeroImagemUrl;

        /**
         * Home hero text.
         */
        private String homeHeroTexto;

        /**
         * Home hero video URL.
         */
        private String homeHeroVideoUrl;

        /**
         * Main contact email.
         */
        private String email;

        /**
         * Main contact phone.
         */
        private String telefone;

        /**
         * Whatsapp contact.
         */
        private String whatsapp;

        /**
         * Instagram account.
         */
        private String instagram;

        /**
         * Official site URL.
         */
        private String siteUrl;

        /**
         * Street address.
         */
        private String endereco;

        /**
         * City name.
         */
        private String cidade;

        /**
         * State code or name.
         */
        private String estado;

        /**
         * ZIP code.
         */
        private String cep;

        /**
         * District name.
         */
        private String bairro;

        /**
         * Customer service schedule.
         */
        private String horarioAtendimento;

        /**
         * Delivery fee value.
         */
        private String taxaEntrega;

        /**
         * Minimum order value.
         */
        private String pedidoMinimo;

        /**
         * Whether pickup is enabled.
         */
        private Boolean retiradaAtiva;

        /**
         * Pickup schedule.
         */
        private String retiradaHorario;

        /**
         * Pickup instructions.
         */
        private String retiradaInstrucoes;

        /**
         * Whether quick signup is enabled.
         */
        private Boolean habilitarCadastrorapido;

        /**
         * Whether subscriptions are enabled.
         */
        private Boolean habilitarAssinaturas;

        /**
         * Whether notifications are enabled.
         */
        private Boolean habilitarNotificacoes;

        /**
         * Whether home highlights are shown.
         */
        private Boolean exibirDestaquesHome;

        /**
         * Whether stock alerts are enabled.
         */
        private Boolean alertaEstoqueAtivo;

        /**
         * Stock alert threshold.
         */
        private String alertaEstoqueLimite;

        /**
         * Stock alert cooldown in minutes.
         */
        private String alertaEstoqueCooldown;

        /**
         * Cron used by stock alert job.
         */
        private String alertaEstoqueCron;
    }
}
