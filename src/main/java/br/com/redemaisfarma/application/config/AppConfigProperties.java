package br.com.redemaisfarma.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Propriedades de configuração da aplicação RedeMaisFarma.
 *
 * <p>
 * Carrega dados do application.yml com o prefixo 'app.config'
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "app.config")
@Validated
public class AppConfigProperties {

    /**
     * Nome do ambiente atual (ex: produção, homologação, dev).
     */
    @NotBlank
    private String envProfileLabel;

    /**
     * Rótulo da porta de rede para exibição em tela.
     */
    @NotBlank
    private String networkPortLabel;

    /**
     * Nome público do sistema (ex: para exibir em header).
     */
    private String systemName;

    /**
     * Versão atual da aplicação.
     */
    private String version;

    /**
     * Mapa de traduções ou textos de sistema que podem ser dinâmicos.
     */
    private Map<String, String> dynamicLabels;

    // Getters e Setters

    public String getEnvProfileLabel() {
        return envProfileLabel;
    }

    public void setEnvProfileLabel(String envProfileLabel) {
        this.envProfileLabel = envProfileLabel;
    }

    public String getNetworkPortLabel() {
        return networkPortLabel;
    }

    public void setNetworkPortLabel(String networkPortLabel) {
        this.networkPortLabel = networkPortLabel;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getDynamicLabels() {
        return dynamicLabels;
    }

    public void setDynamicLabels(Map<String, String> dynamicLabels) {
        this.dynamicLabels = dynamicLabels;
    }
}
