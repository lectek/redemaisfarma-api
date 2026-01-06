/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.ConfigurationProperties
 */
package br.com.redemaisfarma.application.core.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="app.media")
public class ImageStorageProperties {
    private String dir = "media/products";
    private String publicBase = "/media/products";
    private String userDir = "media/users";
    private String userPublicBase = "/media/users";

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getPublicBase() {
        return this.publicBase;
    }

    public void setPublicBase(String publicBase) {
        this.publicBase = publicBase;
    }

    public String getUserDir() {
        return userDir;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public String getUserPublicBase() {
        return userPublicBase;
    }

    public void setUserPublicBase(String userPublicBase) {
        this.userPublicBase = userPublicBase;
    }
}
