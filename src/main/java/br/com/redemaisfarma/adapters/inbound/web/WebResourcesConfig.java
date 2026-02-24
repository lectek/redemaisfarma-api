/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package br.com.redemaisfarma.adapters.inbound.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourcesConfig
implements WebMvcConfigurer {
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[]{"/css/**"}).addResourceLocations(new String[]{"classpath:/static/css/"});
        registry.addResourceHandler(new String[]{"/js/**"}).addResourceLocations(new String[]{"classpath:/static/js/"});
        registry.addResourceHandler(new String[]{"/images/**"}).addResourceLocations(new String[]{"classpath:/static/images/"});
        registry.addResourceHandler(new String[]{"/animations/**"}).addResourceLocations(new String[]{"classpath:/static/animations/"});
        registry.addResourceHandler(new String[]{"/media/**"}).addResourceLocations(new String[]{"file:media/"});
        registry.addResourceHandler(new String[]{"/webjars/**"}).addResourceLocations(new String[]{"classpath:/META-INF/resources/webjars/"});
        registry.addResourceHandler(new String[]{"/favicon.ico"}).addResourceLocations(new String[]{"classpath:/static/favicon.ico"});
        registry.addResourceHandler(new String[]{"/index.html"}).addResourceLocations(new String[]{"classpath:/static/index.html"});
    }
}
