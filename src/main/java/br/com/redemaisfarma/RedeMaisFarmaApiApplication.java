package br.com.redemaisfarma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"br.com.redemaisfarma"})
@ConfigurationPropertiesScan
@EntityScan(basePackages = {"br.com.redemaisfarma"})
@EnableScheduling
public class RedeMaisFarmaApiApplication {
    private static final Logger logger = LoggerFactory.getLogger(RedeMaisFarmaApiApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("🚀 Iniciando aplicação RedeMaisFarma...");
            SpringApplication app = new SpringApplication(RedeMaisFarmaApiApplication.class);
            app.setBannerMode(Banner.Mode.OFF);

            boolean hasActiveProfile =
                    System.getProperty("spring.profiles.active") != null
                    || System.getenv("SPRING_PROFILES_ACTIVE") != null;

            if (!hasActiveProfile) {
                Map<String, Object> defaults = new HashMap<>();
                defaults.put("spring.profiles.default", "dev");
                app.setDefaultProperties(defaults);
            }

            ConfigurableEnvironment env = app.run(args).getEnvironment();
            String port = env.getProperty("server.port", "8080");
            String[] profiles = env.getActiveProfiles();

            logger.info("✅ Aplicação iniciada.");
            logger.info("🌐 URL base: http://localhost:{} (profiles: {})", port, String.join(", ", profiles));
            logger.info("📦 DataSource: {}", env.getProperty("spring.datasource.url",
                    "(multi-datasource em uso ou URL não definida)"));

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> logger.info("🛑 Encerrando aplicação RedeMaisFarma com segurança..."))
            );
        } catch (Exception e) {
            logger.error("❌ Erro ao iniciar a aplicação RedeMaisFarma:", e);
            System.exit(1);
        }
    }
}
