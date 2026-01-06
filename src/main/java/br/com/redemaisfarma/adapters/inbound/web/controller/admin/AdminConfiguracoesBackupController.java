package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.AppSettingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/backup")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesBackupController {

    private static final Path BACKUP_DIR = Paths.get("storage", "backups");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AppSettingRepository repository;
    private final ObjectMapper objectMapper;

    public AdminConfiguracoesBackupController(AppSettingRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("backups", listBackups());
        return "pages/admin/configuracoes/backup";
    }

    @PostMapping("/now")
    public String gerarBackup(RedirectAttributes ra) {
        try {
            Files.createDirectories(BACKUP_DIR);
            BackupPayload payload = new BackupPayload();
            payload.setGeradoEm(LocalDateTime.now());
            payload.setSettings(repository.findAll());
            String filename = "backup-" + FILE_TS.format(LocalDateTime.now()) + ".json";
            Path target = BACKUP_DIR.resolve(filename);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            Files.writeString(target, json, StandardCharsets.UTF_8);
            ra.addFlashAttribute("success", "Backup gerado: " + filename);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Falha ao gerar backup.");
        }
        return "redirect:/admin/configuracoes/backup";
    }

    @PostMapping("/restore/{id}")
    public String restaurar(@PathVariable String id, RedirectAttributes ra) {
        Path target = safeResolve(id);
        if (target == null || !Files.exists(target)) {
            ra.addFlashAttribute("error", "Backup nao encontrado.");
            return "redirect:/admin/configuracoes/backup";
        }
        try (InputStream in = Files.newInputStream(target)) {
            BackupPayload payload = objectMapper.readValue(in, BackupPayload.class);
            if (payload != null && payload.getSettings() != null) {
                for (AppSettingEntity entity : payload.getSettings()) {
                    if (entity.getSettingKey() != null) {
                        repository.findBySettingKey(entity.getSettingKey())
                                .map(existing -> {
                                    existing.setSettingValue(entity.getSettingValue());
                                    existing.setDescription(entity.getDescription());
                                    return repository.save(existing);
                                })
                                .orElseGet(() -> repository.save(new AppSettingEntity(
                                        entity.getSettingKey(),
                                        entity.getSettingValue(),
                                        entity.getDescription()
                                )));
                    }
                }
            }
            ra.addFlashAttribute("success", "Backup restaurado.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Falha ao restaurar backup.");
        }
        return "redirect:/admin/configuracoes/backup";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> baixar(@PathVariable String id) {
        Path target = safeResolve(id);
        if (target == null || !Files.exists(target)) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(target));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + target.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private List<BackupItem> listBackups() {
        if (!Files.exists(BACKUP_DIR)) {
            return List.of();
        }
        List<BackupItem> items = new ArrayList<>();
        try {
            Files.list(BACKUP_DIR)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong(this::safeLastModified).reversed())
                    .forEach(path -> items.add(new BackupItem(path.getFileName().toString(), formatDate(path))));
        } catch (Exception ignored) {
            return List.of();
        }
        return items;
    }

    private long safeLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (Exception ex) {
            return 0L;
        }
    }

    private ZonedDateTime formatDate(Path path) {
        try {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(safeLastModified(path)), ZoneId.systemDefault());
        } catch (Exception ex) {
            return ZonedDateTime.now();
        }
    }

    private Path safeResolve(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        Path candidate = BACKUP_DIR.resolve(Paths.get(id).getFileName()).normalize();
        if (!candidate.startsWith(BACKUP_DIR)) {
            return null;
        }
        return candidate;
    }

    public static class BackupItem {
        private String nome;
        private ZonedDateTime data;

        public BackupItem(String nome, ZonedDateTime data) {
            this.nome = nome;
            this.data = data;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public ZonedDateTime getData() {
            return data;
        }

        public void setData(ZonedDateTime data) {
            this.data = data;
        }
    }

    public static class BackupPayload {
        private LocalDateTime geradoEm;
        private List<AppSettingEntity> settings;

        public LocalDateTime getGeradoEm() {
            return geradoEm;
        }

        public void setGeradoEm(LocalDateTime geradoEm) {
            this.geradoEm = geradoEm;
        }

        public List<AppSettingEntity> getSettings() {
            return settings;
        }

        public void setSettings(List<AppSettingEntity> settings) {
            this.settings = settings;
        }
    }
}
