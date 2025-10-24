/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.lang.NonNull
 *  org.springframework.web.servlet.config.annotation.PathMatchConfigurer
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 *  org.springframework.web.util.pattern.PathPatternParser
 */
package br.com.redemaisfarma.adapters.inbound.web.versioning;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class ApiVersioningConfig {
    @Bean
    public WebMvcConfigurer apiVersioningConfigurer() {
        return new WebMvcConfigurer(this){

            public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
                configurer.setPatternParser(new PathPatternParser());
            }
        };
    }
}

