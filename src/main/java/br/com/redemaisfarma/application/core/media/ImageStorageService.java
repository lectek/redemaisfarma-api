package br.com.redemaisfarma.application.core.media;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@EnableConfigurationProperties(ImageStorageProperties.class)
public class ImageStorageService {

    private final ImageStorageProperties props;

    public ImageStorageService(ImageStorageProperties props) {
        this.props = props;
    }

    public String saveProductImage(Long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("Arquivo vazio");

        String ctype = file.getContentType();
        if (ctype == null || !ctype.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IOException("Tipo de arquivo inválido (somente imagens)");
        }

        Path base = Paths.get(props.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(base);

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String ext = extractExtension(original);
        String ts  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fname = "produto-" + (productId == null ? "na" : productId) + "-" + ts + ext;

        Path target = base.resolve(fname);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return joinPublic(props.getPublicBase(), fname);
        }

    private static String extractExtension(String name) {
        int i = name.lastIndexOf('.');
        String e = i >= 0 ? name.substring(i) : "";
        if (e.length() > 8 || e.contains("/") || e.contains("\\")) e = "";
        return e.toLowerCase(Locale.ROOT);
    }

    private static String joinPublic(String base, String file) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String f = file.startsWith("/") ? file.substring(1) : file;
        return b + "/" + f;
    }
}
