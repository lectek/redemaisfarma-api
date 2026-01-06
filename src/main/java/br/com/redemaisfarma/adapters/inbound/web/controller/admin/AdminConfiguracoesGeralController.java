package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private static final Logger log = LoggerFactory.getLogger(AdminConfiguracoesGeralController.class);

    private static final String KEY_NOME_SISTEMA = "loja.nome";
    private static final String KEY_NOME_FANTASIA = "loja.nome_fantasia";
    private static final String KEY_RAZAO_SOCIAL = "loja.razao_social";
    private static final String KEY_NOME_LOJA_SITE = "loja.nome_exibicao";
    private static final String KEY_SLOGAN_LOJA = "loja.slogan";
    private static final String KEY_LOGO_INICIAL_URL = "branding.logo_url";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_HOME_HERO_IMAGEM_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_TEXTO = "branding.home_hero_texto";
    private static final String KEY_EMAIL = "contato.email";
    private static final String KEY_TELEFONE = "contato.telefone";
    private static final String KEY_WHATSAPP = "contato.whatsapp";
    private static final String KEY_INSTAGRAM = "contato.instagram";
    private static final String KEY_SITE_URL = "contato.site_url";
    private static final String KEY_ENDERECO = "endereco.logradouro";
    private static final String KEY_CIDADE = "endereco.cidade";
    private static final String KEY_ESTADO = "endereco.estado";
    private static final String KEY_CEP = "endereco.cep";
    private static final String KEY_BAIRRO = "endereco.bairro";
    private static final String KEY_HORARIO_ATENDIMENTO = "entrega.horario_atendimento";
    private static final String KEY_TAXA_ENTREGA = "entrega.taxa";
    private static final String KEY_PEDIDO_MINIMO = "entrega.pedido_minimo";
    private static final String KEY_RETIRADA_ATIVA = "retirada.ativa";
    private static final String KEY_RETIRADA_HORARIO = "retirada.horario";
    private static final String KEY_RETIRADA_INSTRUCOES = "retirada.instrucoes";
    private static final String KEY_HABILITAR_CADASTRO_RAPIDO = "preferencias.cadastro_rapido";
    private static final String KEY_HABILITAR_ASSINATURAS = "preferencias.assinaturas";
    private static final String KEY_HABILITAR_NOTIFICACOES = "preferencias.notificacoes";
    private static final String KEY_EXIBIR_DESTAQUES_HOME = "preferencias.exibir_destaques_home";
    private static final String KEY_ALERTA_ESTOQUE_ENABLED = "app.estoque.alerta.enabled";
    private static final String KEY_ALERTA_ESTOQUE_LIMITE = "app.estoque.alerta.limite";
    private static final String KEY_ALERTA_ESTOQUE_COOLDOWN = "app.estoque.alerta.cooldown-minutes";
    private static final String KEY_ALERTA_ESTOQUE_CRON = "app.estoque.alerta.cron";

    private static final String LEGACY_NOME_SISTEMA = "GERAL.nome_sistema";
    private static final String LEGACY_NOME_FANTASIA = "GERAL.nome_fantasia";
    private static final String LEGACY_RAZAO_SOCIAL = "GERAL.razao_social";
    private static final String LEGACY_NOME_LOJA_SITE = "GERAL.nome_loja_site";
    private static final String LEGACY_SLOGAN_LOJA = "GERAL.slogan_loja";
    private static final String LEGACY_LOGO_INICIAL_URL = "GERAL.logo_inicial_url";
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";
    private static final String LEGACY_HOME_HERO_IMAGEM_URL = "GERAL.home_hero_imagem_url";
    private static final String LEGACY_HOME_HERO_TEXTO = "GERAL.home_hero_texto";
    private static final String LEGACY_EMAIL = "GERAL.email";
    private static final String LEGACY_TELEFONE = "GERAL.telefone";
    private static final String LEGACY_WHATSAPP = "GERAL.whatsapp";
    private static final String LEGACY_INSTAGRAM = "GERAL.instagram";
    private static final String LEGACY_SITE_URL = "GERAL.site_url";
    private static final String LEGACY_ENDERECO = "GERAL.endereco";
    private static final String LEGACY_CIDADE = "GERAL.cidade";
    private static final String LEGACY_ESTADO = "GERAL.estado";
    private static final String LEGACY_CEP = "GERAL.cep";
    private static final String LEGACY_BAIRRO = "GERAL.bairro";
    private static final String LEGACY_HORARIO_ATENDIMENTO = "GERAL.horario_atendimento";
    private static final String LEGACY_TAXA_ENTREGA = "GERAL.taxa_entrega";
    private static final String LEGACY_PEDIDO_MINIMO = "GERAL.pedido_minimo";
    private static final String LEGACY_HABILITAR_CADASTRO_RAPIDO = "GERAL.habilitar_cadastro_rapido";
    private static final String LEGACY_HABILITAR_ASSINATURAS = "GERAL.habilitar_assinaturas";
    private static final String LEGACY_HABILITAR_NOTIFICACOES = "GERAL.habilitar_notificacoes";
    private static final String LEGACY_EXIBIR_DESTAQUES_HOME = "GERAL.exibir_destaques_home";

    private static final Set<String> ALL_KEYS = Set.of(
            KEY_NOME_SISTEMA,
            KEY_NOME_FANTASIA,
            KEY_RAZAO_SOCIAL,
            KEY_NOME_LOJA_SITE,
            KEY_SLOGAN_LOJA,
            KEY_LOGO_INICIAL_URL,
            KEY_FAVICON_URL,
            KEY_HOME_HERO_IMAGEM_URL,
            KEY_HOME_HERO_TEXTO,
            KEY_EMAIL,
            KEY_TELEFONE,
            KEY_WHATSAPP,
            KEY_INSTAGRAM,
            KEY_SITE_URL,
            KEY_ENDERECO,
            KEY_CIDADE,
            KEY_ESTADO,
            KEY_CEP,
            KEY_BAIRRO,
            KEY_HORARIO_ATENDIMENTO,
            KEY_TAXA_ENTREGA,
            KEY_PEDIDO_MINIMO,
            KEY_RETIRADA_ATIVA,
            KEY_RETIRADA_HORARIO,
            KEY_RETIRADA_INSTRUCOES,
            KEY_HABILITAR_CADASTRO_RAPIDO,
            KEY_HABILITAR_ASSINATURAS,
            KEY_HABILITAR_NOTIFICACOES,
            KEY_EXIBIR_DESTAQUES_HOME,
            KEY_ALERTA_ESTOQUE_ENABLED,
            KEY_ALERTA_ESTOQUE_LIMITE,
            KEY_ALERTA_ESTOQUE_COOLDOWN,
            KEY_ALERTA_ESTOQUE_CRON,
            LEGACY_NOME_SISTEMA,
            LEGACY_NOME_FANTASIA,
            LEGACY_RAZAO_SOCIAL,
            LEGACY_NOME_LOJA_SITE,
            LEGACY_SLOGAN_LOJA,
            LEGACY_LOGO_INICIAL_URL,
            LEGACY_FAVICON_URL,
            LEGACY_HOME_HERO_IMAGEM_URL,
            LEGACY_HOME_HERO_TEXTO,
            LEGACY_EMAIL,
            LEGACY_TELEFONE,
            LEGACY_WHATSAPP,
            LEGACY_INSTAGRAM,
            LEGACY_SITE_URL,
            LEGACY_ENDERECO,
            LEGACY_CIDADE,
            LEGACY_ESTADO,
            LEGACY_CEP,
            LEGACY_BAIRRO,
            LEGACY_HORARIO_ATENDIMENTO,
            LEGACY_TAXA_ENTREGA,
            LEGACY_PEDIDO_MINIMO,
            LEGACY_HABILITAR_CADASTRO_RAPIDO,
            LEGACY_HABILITAR_ASSINATURAS,
            LEGACY_HABILITAR_NOTIFICACOES,
            LEGACY_EXIBIR_DESTAQUES_HOME
    );

    private final AppSettingService settings;

    public AdminConfiguracoesGeralController(AppSettingService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("cfg", loadForm());
        return "pages/admin/configuracoes/geral";
    }

    @PostMapping
    public String salvar(@ModelAttribute("cfg") ConfigGeralForm cfg,
                         @RequestParam(name = "logoInicialFile", required = false) MultipartFile logoInicialFile,
                         @RequestParam(name = "homeHeroImagem", required = false) MultipartFile homeHeroImagem,
                         @RequestParam(name = "section", required = false) String section,
                         RedirectAttributes ra) {
        boolean saveAll = section == null || section.isBlank();
        boolean uploadFailed = false;
        List<String> uploadErrors = new ArrayList<>();
        if (saveAll || "identidade".equals(section)) {
            if (logoInicialFile != null && !logoInicialFile.isEmpty()) {
                try {
                    cfg.setLogoInicialUrl(storeUpload(logoInicialFile, "logo"));
                } catch (IOException ex) {
                    uploadFailed = true;
                    String msg = "Falha ao salvar logo inicial.";
                    uploadErrors.add(msg);
                    log.warn(msg, ex);
                }
            }
            if (homeHeroImagem != null && !homeHeroImagem.isEmpty()) {
                try {
                    cfg.setHomeHeroImagemUrl(storeUpload(homeHeroImagem, "home-hero"));
                } catch (IOException ex) {
                    uploadFailed = true;
                    String msg = "Falha ao salvar imagem do hero.";
                    uploadErrors.add(msg);
                    log.warn(msg, ex);
                }
            }
            settings.upsert(KEY_NOME_SISTEMA, nullSafe(cfg.getNomeSistema()), "Nome do sistema");
            settings.upsert(KEY_NOME_FANTASIA, nullSafe(cfg.getNomeFantasia()), "Nome fantasia");
            settings.upsert(KEY_RAZAO_SOCIAL, nullSafe(cfg.getRazaoSocial()), "Razao social");
            settings.upsert(KEY_NOME_LOJA_SITE, nullSafe(cfg.getNomeLojaSite()), "Nome exibido no site");
            settings.upsert(KEY_SLOGAN_LOJA, nullSafe(cfg.getSloganLoja()), "Slogan");
            settings.upsert(KEY_LOGO_INICIAL_URL, nullSafe(cfg.getLogoInicialUrl()), "Logo inicial");
            settings.upsert(KEY_FAVICON_URL, nullSafe(cfg.getFaviconUrl()), "Favicon");
            settings.upsert(KEY_HOME_HERO_IMAGEM_URL, nullSafe(cfg.getHomeHeroImagemUrl()), "Imagem principal da home");
            settings.upsert(KEY_HOME_HERO_TEXTO, nullSafe(cfg.getHomeHeroTexto()), "Texto do destaque");
        }
        if (saveAll || "contato".equals(section)) {
            settings.upsert(KEY_EMAIL, nullSafe(cfg.getEmail()), "Email principal");
            settings.upsert(KEY_TELEFONE, nullSafe(cfg.getTelefone()), "Telefone principal");
            settings.upsert(KEY_WHATSAPP, nullSafe(cfg.getWhatsapp()), "Whatsapp");
            settings.upsert(KEY_INSTAGRAM, nullSafe(cfg.getInstagram()), "Instagram");
            settings.upsert(KEY_SITE_URL, nullSafe(cfg.getSiteUrl()), "Site oficial");
        }
        if (saveAll || "endereco".equals(section)) {
            settings.upsert(KEY_ENDERECO, nullSafe(cfg.getEndereco()), "Endereco");
            settings.upsert(KEY_CIDADE, nullSafe(cfg.getCidade()), "Cidade");
            settings.upsert(KEY_ESTADO, nullSafe(cfg.getEstado()), "Estado");
            settings.upsert(KEY_CEP, nullSafe(cfg.getCep()), "CEP");
            settings.upsert(KEY_BAIRRO, nullSafe(cfg.getBairro()), "Bairro");
        }
        if (saveAll || "entrega".equals(section)) {
            settings.upsert(KEY_HORARIO_ATENDIMENTO, nullSafe(cfg.getHorarioAtendimento()), "Horario de atendimento");
            settings.upsert(KEY_TAXA_ENTREGA, nullSafe(cfg.getTaxaEntrega()), "Taxa de entrega");
            settings.upsert(KEY_PEDIDO_MINIMO, nullSafe(cfg.getPedidoMinimo()), "Pedido minimo");
            settings.upsert(KEY_RETIRADA_ATIVA, bool(cfg.getRetiradaAtiva()), "Retirada na loja ativa");
            settings.upsert(KEY_RETIRADA_HORARIO, nullSafe(cfg.getRetiradaHorario()), "Horario de retirada");
            settings.upsert(KEY_RETIRADA_INSTRUCOES, nullSafe(cfg.getRetiradaInstrucoes()), "Instrucoes de retirada");
        }
        if (saveAll || "preferencias".equals(section)) {
            settings.upsert(KEY_HABILITAR_CADASTRO_RAPIDO, bool(cfg.getHabilitarCadastrorapido()), "Cadastro rapido");
            settings.upsert(KEY_HABILITAR_ASSINATURAS, bool(cfg.getHabilitarAssinaturas()), "Assinaturas");
            settings.upsert(KEY_HABILITAR_NOTIFICACOES, bool(cfg.getHabilitarNotificacoes()), "Notificacoes");
            settings.upsert(KEY_EXIBIR_DESTAQUES_HOME, bool(cfg.getExibirDestaquesHome()), "Destaques na home");
        }
        if (saveAll || "alertas".equals(section)) {
            settings.upsert(KEY_ALERTA_ESTOQUE_ENABLED, bool(cfg.getAlertaEstoqueAtivo()), "Alerta estoque ativo");
            settings.upsert(KEY_ALERTA_ESTOQUE_LIMITE, nullSafe(cfg.getAlertaEstoqueLimite()), "Alerta estoque limite");
            settings.upsert(KEY_ALERTA_ESTOQUE_COOLDOWN, nullSafe(cfg.getAlertaEstoqueCooldown()), "Alerta estoque cooldown (min)");
            settings.upsert(KEY_ALERTA_ESTOQUE_CRON, nullSafe(cfg.getAlertaEstoqueCron()), "Alerta estoque cron");
        }

        if (uploadFailed) {
            String joined = uploadErrors.isEmpty()
                    ? "Alguns uploads falharam; verifique os arquivos enviados."
                    : String.join(" ", uploadErrors);
            ra.addFlashAttribute("warning", joined);
        } else {
            ra.addFlashAttribute("success", "Configuracoes gerais atualizadas.");
        }
        return "redirect:/admin/configuracoes/geral";
    }

    private ConfigGeralForm loadForm() {
        Map<String, String> cfg = settings.getAllByKeys(ALL_KEYS);
        ConfigGeralForm form = new ConfigGeralForm();
        form.setNomeSistema(firstValue(cfg, KEY_NOME_SISTEMA, LEGACY_NOME_SISTEMA));
        form.setNomeFantasia(firstValue(cfg, KEY_NOME_FANTASIA, LEGACY_NOME_FANTASIA));
        form.setRazaoSocial(firstValue(cfg, KEY_RAZAO_SOCIAL, LEGACY_RAZAO_SOCIAL));
        form.setNomeLojaSite(firstValue(cfg, KEY_NOME_LOJA_SITE, LEGACY_NOME_LOJA_SITE));
        form.setSloganLoja(firstValue(cfg, KEY_SLOGAN_LOJA, LEGACY_SLOGAN_LOJA));
        form.setLogoInicialUrl(firstValue(cfg, KEY_LOGO_INICIAL_URL, LEGACY_LOGO_INICIAL_URL));
        form.setFaviconUrl(firstValue(cfg, KEY_FAVICON_URL, LEGACY_FAVICON_URL));
        form.setHomeHeroImagemUrl(firstValue(cfg, KEY_HOME_HERO_IMAGEM_URL, LEGACY_HOME_HERO_IMAGEM_URL));
        form.setHomeHeroTexto(firstValue(cfg, KEY_HOME_HERO_TEXTO, LEGACY_HOME_HERO_TEXTO));
        form.setEmail(firstValue(cfg, KEY_EMAIL, LEGACY_EMAIL));
        form.setTelefone(firstValue(cfg, KEY_TELEFONE, LEGACY_TELEFONE));
        form.setWhatsapp(firstValue(cfg, KEY_WHATSAPP, LEGACY_WHATSAPP));
        form.setInstagram(firstValue(cfg, KEY_INSTAGRAM, LEGACY_INSTAGRAM));
        form.setSiteUrl(firstValue(cfg, KEY_SITE_URL, LEGACY_SITE_URL));
        form.setEndereco(firstValue(cfg, KEY_ENDERECO, LEGACY_ENDERECO));
        form.setCidade(firstValue(cfg, KEY_CIDADE, LEGACY_CIDADE));
        form.setEstado(firstValue(cfg, KEY_ESTADO, LEGACY_ESTADO));
        form.setCep(firstValue(cfg, KEY_CEP, LEGACY_CEP));
        form.setBairro(firstValue(cfg, KEY_BAIRRO, LEGACY_BAIRRO));
        form.setHorarioAtendimento(firstValue(cfg, KEY_HORARIO_ATENDIMENTO, LEGACY_HORARIO_ATENDIMENTO));
        form.setTaxaEntrega(firstValue(cfg, KEY_TAXA_ENTREGA, LEGACY_TAXA_ENTREGA));
        form.setPedidoMinimo(firstValue(cfg, KEY_PEDIDO_MINIMO, LEGACY_PEDIDO_MINIMO));
        form.setRetiradaAtiva(settings.getBoolean(KEY_RETIRADA_ATIVA, false));
        form.setRetiradaHorario(settings.getOrDefault(KEY_RETIRADA_HORARIO, ""));
        form.setRetiradaInstrucoes(settings.getOrDefault(KEY_RETIRADA_INSTRUCOES, ""));
        form.setHabilitarCadastrorapido(readBooleanWithFallback(KEY_HABILITAR_CADASTRO_RAPIDO, LEGACY_HABILITAR_CADASTRO_RAPIDO));
        form.setHabilitarAssinaturas(readBooleanWithFallback(KEY_HABILITAR_ASSINATURAS, LEGACY_HABILITAR_ASSINATURAS));
        form.setHabilitarNotificacoes(readBooleanWithFallback(KEY_HABILITAR_NOTIFICACOES, LEGACY_HABILITAR_NOTIFICACOES));
        form.setExibirDestaquesHome(readBooleanWithFallback(KEY_EXIBIR_DESTAQUES_HOME, LEGACY_EXIBIR_DESTAQUES_HOME));
        form.setAlertaEstoqueAtivo(settings.getBoolean(KEY_ALERTA_ESTOQUE_ENABLED, true));
        form.setAlertaEstoqueLimite(String.valueOf(settings.getInt(KEY_ALERTA_ESTOQUE_LIMITE, 10)));
        form.setAlertaEstoqueCooldown(String.valueOf(settings.getInt(KEY_ALERTA_ESTOQUE_COOLDOWN, 60)));
        form.setAlertaEstoqueCron(settings.getOrDefault(KEY_ALERTA_ESTOQUE_CRON, "0 */30 * * * *"));
        return form;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String firstValue(Map<String, String> cfg, String primary, String fallback) {
        String value = cfg.get(primary);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return cfg.getOrDefault(fallback, "");
    }

    private boolean readBooleanWithFallback(String primary, String fallback) {
        return settings.get(primary)
                .map(AdminConfiguracoesGeralController::parseBoolean)
                .orElseGet(() -> settings.getBoolean(fallback, false));
    }

    private static boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        String s = value.trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
    }

    private static String bool(Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    private static String storeUpload(MultipartFile file, String prefix) throws IOException {
        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "upload"));
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }
        String filename = prefix + "-" + System.currentTimeMillis() + ext;
        Path dir = Paths.get("media", "branding");
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/media/branding/" + filename;
    }

    public static class ConfigGeralForm {
        private String nomeSistema;
        private String nomeFantasia;
        private String razaoSocial;
        private String nomeLojaSite;
        private String sloganLoja;
        private String logoInicialUrl;
        private String faviconUrl;
        private String homeHeroImagemUrl;
        private String homeHeroTexto;
        private String email;
        private String telefone;
        private String whatsapp;
        private String instagram;
        private String siteUrl;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private String bairro;
        private String horarioAtendimento;
        private String taxaEntrega;
        private String pedidoMinimo;
        private Boolean retiradaAtiva;
        private String retiradaHorario;
        private String retiradaInstrucoes;
        private Boolean habilitarCadastrorapido;
        private Boolean habilitarAssinaturas;
        private Boolean habilitarNotificacoes;
        private Boolean exibirDestaquesHome;
        private Boolean alertaEstoqueAtivo;
        private String alertaEstoqueLimite;
        private String alertaEstoqueCooldown;
        private String alertaEstoqueCron;

        public String getNomeSistema() {
            return nomeSistema;
        }

        public void setNomeSistema(String nomeSistema) {
            this.nomeSistema = nomeSistema;
        }

        public String getNomeFantasia() {
            return nomeFantasia;
        }

        public void setNomeFantasia(String nomeFantasia) {
            this.nomeFantasia = nomeFantasia;
        }

        public String getRazaoSocial() {
            return razaoSocial;
        }

        public void setRazaoSocial(String razaoSocial) {
            this.razaoSocial = razaoSocial;
        }

        public String getNomeLojaSite() {
            return nomeLojaSite;
        }

        public void setNomeLojaSite(String nomeLojaSite) {
            this.nomeLojaSite = nomeLojaSite;
        }

        public String getSloganLoja() {
            return sloganLoja;
        }

        public void setSloganLoja(String sloganLoja) {
            this.sloganLoja = sloganLoja;
        }

        public String getLogoInicialUrl() {
            return logoInicialUrl;
        }

        public void setLogoInicialUrl(String logoInicialUrl) {
            this.logoInicialUrl = logoInicialUrl;
        }

        public String getFaviconUrl() {
            return faviconUrl;
        }

        public void setFaviconUrl(String faviconUrl) {
            this.faviconUrl = faviconUrl;
        }

        public String getHomeHeroImagemUrl() {
            return homeHeroImagemUrl;
        }

        public void setHomeHeroImagemUrl(String homeHeroImagemUrl) {
            this.homeHeroImagemUrl = homeHeroImagemUrl;
        }

        public String getHomeHeroTexto() {
            return homeHeroTexto;
        }

        public void setHomeHeroTexto(String homeHeroTexto) {
            this.homeHeroTexto = homeHeroTexto;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public void setWhatsapp(String whatsapp) {
            this.whatsapp = whatsapp;
        }

        public String getInstagram() {
            return instagram;
        }

        public void setInstagram(String instagram) {
            this.instagram = instagram;
        }

        public String getSiteUrl() {
            return siteUrl;
        }

        public void setSiteUrl(String siteUrl) {
            this.siteUrl = siteUrl;
        }

        public String getEndereco() {
            return endereco;
        }

        public void setEndereco(String endereco) {
            this.endereco = endereco;
        }

        public String getCidade() {
            return cidade;
        }

        public void setCidade(String cidade) {
            this.cidade = cidade;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getCep() {
            return cep;
        }

        public void setCep(String cep) {
            this.cep = cep;
        }

        public String getBairro() {
            return bairro;
        }

        public void setBairro(String bairro) {
            this.bairro = bairro;
        }

        public String getHorarioAtendimento() {
            return horarioAtendimento;
        }

        public void setHorarioAtendimento(String horarioAtendimento) {
            this.horarioAtendimento = horarioAtendimento;
        }

        public String getTaxaEntrega() {
            return taxaEntrega;
        }

        public void setTaxaEntrega(String taxaEntrega) {
            this.taxaEntrega = taxaEntrega;
        }

        public String getPedidoMinimo() {
            return pedidoMinimo;
        }

        public void setPedidoMinimo(String pedidoMinimo) {
            this.pedidoMinimo = pedidoMinimo;
        }

        public Boolean getRetiradaAtiva() {
            return retiradaAtiva;
        }

        public void setRetiradaAtiva(Boolean retiradaAtiva) {
            this.retiradaAtiva = retiradaAtiva;
        }

        public String getRetiradaHorario() {
            return retiradaHorario;
        }

        public void setRetiradaHorario(String retiradaHorario) {
            this.retiradaHorario = retiradaHorario;
        }

        public String getRetiradaInstrucoes() {
            return retiradaInstrucoes;
        }

        public void setRetiradaInstrucoes(String retiradaInstrucoes) {
            this.retiradaInstrucoes = retiradaInstrucoes;
        }

        public Boolean getHabilitarCadastrorapido() {
            return habilitarCadastrorapido;
        }

        public void setHabilitarCadastrorapido(Boolean habilitarCadastrorapido) {
            this.habilitarCadastrorapido = habilitarCadastrorapido;
        }

        public Boolean getHabilitarAssinaturas() {
            return habilitarAssinaturas;
        }

        public void setHabilitarAssinaturas(Boolean habilitarAssinaturas) {
            this.habilitarAssinaturas = habilitarAssinaturas;
        }

        public Boolean getHabilitarNotificacoes() {
            return habilitarNotificacoes;
        }

        public void setHabilitarNotificacoes(Boolean habilitarNotificacoes) {
            this.habilitarNotificacoes = habilitarNotificacoes;
        }

        public Boolean getExibirDestaquesHome() {
            return exibirDestaquesHome;
        }

        public void setExibirDestaquesHome(Boolean exibirDestaquesHome) {
            this.exibirDestaquesHome = exibirDestaquesHome;
        }

        public Boolean getAlertaEstoqueAtivo() {
            return alertaEstoqueAtivo;
        }

        public void setAlertaEstoqueAtivo(Boolean alertaEstoqueAtivo) {
            this.alertaEstoqueAtivo = alertaEstoqueAtivo;
        }

        public String getAlertaEstoqueLimite() {
            return alertaEstoqueLimite;
        }

        public void setAlertaEstoqueLimite(String alertaEstoqueLimite) {
            this.alertaEstoqueLimite = alertaEstoqueLimite;
        }

        public String getAlertaEstoqueCooldown() {
            return alertaEstoqueCooldown;
        }

        public void setAlertaEstoqueCooldown(String alertaEstoqueCooldown) {
            this.alertaEstoqueCooldown = alertaEstoqueCooldown;
        }

        public String getAlertaEstoqueCron() {
            return alertaEstoqueCron;
        }

        public void setAlertaEstoqueCron(String alertaEstoqueCron) {
            this.alertaEstoqueCron = alertaEstoqueCron;
        }
    }
}
