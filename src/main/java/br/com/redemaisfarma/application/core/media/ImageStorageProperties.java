package br.com.redemaisfarma.application.core.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public class ImageStorageProperties {
    /** Diretório físico onde os arquivos ficam */
    private String dir = "media/products";
    /** Prefixo público para servir as imagens (ex.: via static mapping / media controller / nginx) */
    private String publicBase = "/media/products";

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
    public String getPublicBase() { return publicBase; }
    public void setPublicBase(String publicBase) { this.publicBase = publicBase; }
}
