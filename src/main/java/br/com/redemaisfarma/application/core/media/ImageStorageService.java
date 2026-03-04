package br.com.redemaisfarma.application.core.media;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@EnableConfigurationProperties(ImageStorageProperties.class)
public class ImageStorageService {

    private final ImageStorageProperties props;
    private static final long MAX_IMAGE_BYTES = 2L * 1024L * 1024L;

    public ImageStorageService(ImageStorageProperties props) {
        this.props = props;
    }

    public String saveProductImage(Long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("Arquivo vazio");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new IOException("Imagem acima de 2MB");

        String ctype = file.getContentType();
        if (ctype == null || !isAllowedImageType(ctype)) {
            throw new IOException("Tipo de arquivo invalido (somente PNG, JPG ou WEBP)");
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

    public String saveUserAvatar(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("Arquivo vazio");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new IOException("Imagem acima de 2MB");

        String ctype = file.getContentType();
        if (ctype == null || !isAllowedImageType(ctype)) {
            throw new IOException("Tipo de arquivo invalido (somente PNG, JPG ou WEBP)");
        }

        Path base = Paths.get(props.getUserDir()).toAbsolutePath().normalize();
        Files.createDirectories(base);

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String ext = extractExtension(original);
        String ts  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fname = "usuario-" + (userId == null ? "na" : userId) + "-" + ts + ext;

        Path target = base.resolve(fname);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return joinPublic(props.getUserPublicBase(), fname);
    }

    public String saveProductImagePng(Long productId, BufferedImage image)
            throws IOException {
        if (image == null) {
            throw new IOException("Imagem invalida para persistencia.");
        }

        Path base = Paths.get(props.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(base);

        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fname = "produto-"
                + (productId == null ? "na" : productId)
                + "-"
                + ts
                + ".png";

        Path target = base.resolve(fname);
        try (OutputStream out = Files.newOutputStream(
                target,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            if (!ImageIO.write(image, "png", out)) {
                throw new IOException("Falha ao converter imagem para PNG.");
            }
        }

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

    private static boolean isAllowedImageType(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return normalized.equals("image/jpeg")
                || normalized.equals("image/png")
                || normalized.equals("image/webp");
    }
}
