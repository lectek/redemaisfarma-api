package br.com.redemaisfarma.adapters.outbound.storage.adapter;

import br.com.redemaisfarma.adapters.outbound.storage.exception.StorageException;
import br.com.redemaisfarma.adapters.outbound.storage.model.StoredObject;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalStorageAdapter {

    private final Path base;

    public LocalStorageAdapter(String basePath) {
        this.base = Path.of(basePath == null || basePath.isBlank() ? "storage" : basePath).toAbsolutePath();
        try {
            Files.createDirectories(this.base);
        } catch (IOException ignored) {
        }
    }

    public String upload(InputStream in, long contentLength, String contentType, String key) {
        try {
            if (key == null || key.isBlank())
                throw new IllegalArgumentException("key vazio");
            Path target = base.resolve(key).normalize();
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            if (contentType != null) {
                Files.writeString(target.resolveSibling(target.getFileName() + ".ct"), contentType);
            }
            return key;
        } catch (IOException e) {
            throw new StorageException("Falha ao salvar arquivo local", e);
        }
    }

    public StoredObject download(String key) {
        try {
            Path p = base.resolve(key).normalize();
            String ct = null;
            Path ctFile = p.resolveSibling(p.getFileName() + ".ct");
            if (Files.exists(ctFile))
                ct = Files.readString(ctFile);
            return new StoredObject(key, ct, Files.size(p), Files.newInputStream(p));
        } catch (IOException e) {
            throw new StorageException("Falha ao ler arquivo local", e);
        }
    }

    public void delete(String key) {
        try {
            Path p = base.resolve(key).normalize();
            Files.deleteIfExists(p);
            Files.deleteIfExists(p.resolveSibling(p.getFileName() + ".ct"));
        } catch (IOException e) {
            throw new StorageException("Falha ao excluir arquivo local", e);
        }
    }

    public void purgeAll() {
        try {
            FileSystemUtils.deleteRecursively(base);
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new StorageException("Falha ao limpar storage local", e);
        }
    }
}
