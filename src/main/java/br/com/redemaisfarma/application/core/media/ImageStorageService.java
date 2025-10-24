/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 *  org.springframework.web.multipart.MultipartFile
 */
package br.com.redemaisfarma.application.core.media;

import br.com.redemaisfarma.application.core.media.ImageStorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@EnableConfigurationProperties(value={ImageStorageProperties.class})
public class ImageStorageService {
    private final ImageStorageProperties props;

    public ImageStorageService(ImageStorageProperties props) {
        this.props = props;
    }

    public String saveProductImage(Long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }
        String ctype = file.getContentType();
        if (ctype == null || !ctype.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IOException("Tipo de arquivo inv\u00e1lido (somente imagens)");
        }
        Path base = Paths.get(this.props.getDir(), new String[0]).toAbsolutePath().normalize();
        Files.createDirectories(base, new FileAttribute[0]);
        String original = StringUtils.cleanPath((String)(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename()));
        String ext = ImageStorageService.extractExtension(original);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fname = "produto-" + String.valueOf(productId == null ? "na" : productId) + "-" + ts + ext;
        Path target = base.resolve(fname);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String publicUrl = ImageStorageService.joinPublic(this.props.getPublicBase(), fname);
        return publicUrl;
    }

    private static String extractExtension(String name) {
        String e;
        int i = name.lastIndexOf(46);
        String string = e = i >= 0 ? name.substring(i) : "";
        if (e.length() > 8 || e.contains("/") || e.contains("\\")) {
            e = "";
        }
        return e.toLowerCase(Locale.ROOT);
    }

    private static String joinPublic(String base, String file) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String f = file.startsWith("/") ? file.substring(1) : file;
        return b + "/" + f;
    }
}

