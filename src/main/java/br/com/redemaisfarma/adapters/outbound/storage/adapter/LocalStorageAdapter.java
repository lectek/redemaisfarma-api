/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.util.FileSystemUtils
 */
package br.com.redemaisfarma.adapters.outbound.storage.adapter;

import br.com.redemaisfarma.adapters.outbound.storage.exception.StorageException;
import br.com.redemaisfarma.adapters.outbound.storage.model.StoredObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import org.springframework.util.FileSystemUtils;

public class LocalStorageAdapter {
    private final Path base;

    public LocalStorageAdapter(String basePath) {
        this.base = Path.of(basePath == null || basePath.isBlank() ? "storage" : basePath, new String[0]).toAbsolutePath();
        try {
            Files.createDirectories(this.base, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public String upload(InputStream in, long contentLength, String contentType, String key) {
        try {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("key vazio");
            }
            Path target = this.base.resolve(key).normalize();
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            if (contentType != null) {
                Files.writeString(target.resolveSibling(String.valueOf(target.getFileName()) + ".ct"), (CharSequence)contentType, new OpenOption[0]);
            }
            return key;
        }
        catch (IOException e) {
            throw new StorageException("Falha ao salvar arquivo local", e);
        }
    }

    public StoredObject download(String key) {
        try {
            Path p = this.base.resolve(key).normalize();
            String ct = null;
            Path ctFile = p.resolveSibling(String.valueOf(p.getFileName()) + ".ct");
            if (Files.exists(ctFile, new LinkOption[0])) {
                ct = Files.readString(ctFile);
            }
            return new StoredObject(key, ct, Files.size(p), Files.newInputStream(p, new OpenOption[0]));
        }
        catch (IOException e) {
            throw new StorageException("Falha ao ler arquivo local", e);
        }
    }

    public void delete(String key) {
        try {
            Path p = this.base.resolve(key).normalize();
            Files.deleteIfExists(p);
            Files.deleteIfExists(p.resolveSibling(String.valueOf(p.getFileName()) + ".ct"));
        }
        catch (IOException e) {
            throw new StorageException("Falha ao excluir arquivo local", e);
        }
    }

    public void purgeAll() {
        try {
            FileSystemUtils.deleteRecursively((Path)this.base);
            Files.createDirectories(this.base, new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new StorageException("Falha ao limpar storage local", e);
        }
    }
}

