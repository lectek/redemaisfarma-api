/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.Banner$Mode
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.boot.autoconfigure.domain.EntityScan
 *  org.springframework.boot.context.properties.ConfigurationPropertiesScan
 *  org.springframework.core.env.ConfigurableEnvironment
 */
package br.com.redemaisfarma;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication(scanBasePackages={"br.com.redemaisfarma"})
@ConfigurationPropertiesScan
@EntityScan(basePackages={"br.com.redemaisfarma"})
public class RedeMaisFarmaApiApplication {
    private static final Logger logger = LoggerFactory.getLogger(RedeMaisFarmaApiApplication.class);

    public static void main(String[] args) {
        try {
            boolean hasActiveProfile;
            logger.info("\ud83d\ude80 Iniciando aplica\u00e7\u00e3o RedeMaisFarma...");
            SpringApplication app = new SpringApplication(new Class[]{RedeMaisFarmaApiApplication.class});
            app.setBannerMode(Banner.Mode.OFF);
            boolean bl = hasActiveProfile = System.getProperty("spring.profiles.active") != null || System.getenv("SPRING_PROFILES_ACTIVE") != null;
            if (!hasActiveProfile) {
                HashMap<String, Object> defaults = new HashMap<String, Object>();
                defaults.put("spring.profiles.default", "dev");
                app.setDefaultProperties(defaults);
            }
            ConfigurableEnvironment env = app.run(args).getEnvironment();
            String port = env.getProperty("server.port", "8080");
            CharSequence[] profiles = env.getActiveProfiles();
            logger.info("\u2705 Aplica\u00e7\u00e3o iniciada.");
            logger.info("\ud83c\udf10 URL base: http://localhost:{} (profiles: {})", (Object)port, (Object)String.join((CharSequence)", ", profiles));
            logger.info("\ud83d\udce6 DataSource: {}", (Object)env.getProperty("spring.datasource.url", "(multi-datasource em uso ou URL n\u00e3o definida)"));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("\ud83d\uded1 Encerrando aplica\u00e7\u00e3o RedeMaisFarma com seguran\u00e7a...")));
        }
        catch (Exception e) {
            logger.error("\u274c Erro ao iniciar a aplica\u00e7\u00e3o RedeMaisFarma:", (Throwable)e);
            System.exit(1);
        }
    }
}


