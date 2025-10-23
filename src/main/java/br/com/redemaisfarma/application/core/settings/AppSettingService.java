// src/main/java/br/com/redemaisfarma/application/core/settings/AppSettingService.java
package br.com.redemaisfarma.application.core.settings;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.AppSettingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AppSettingService {

    private final AppSettingRepository repository;

    public AppSettingService(AppSettingRepository repository) {
        this.repository = repository;
    }

    // ========= CRUD / LISTAGEM =========

    @Transactional(readOnly = true)
    public Page<AppSettingEntity> list(String q, Pageable pageable) {
        return repository.search(q, pageable);
    }

    @Transactional
    public AppSettingEntity create(String key, String value, String description) {
        if (repository.existsBySettingKey(key)) {
            throw new IllegalArgumentException("Já existe uma configuração com a chave: " + key);
        }
        var entity = new AppSettingEntity(key, value, description);
        return repository.save(entity);
    }

    @Transactional
    public AppSettingEntity update(Long id, String key, String value, String description) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Config não encontrada: id=" + id));

        if (!entity.getSettingKey().equals(key) && repository.existsBySettingKey(key)) {
            throw new IllegalArgumentException("Já existe uma configuração com a chave: " + key);
        }

        entity.setSettingKey(key);
        entity.setSettingValue(value);
        entity.setDescription(description);
        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AppSettingEntity> findById(Long id) {
        return repository.findById(id);
    }

    // ========= LEITURA (String) =========

    /** Retorna valor bruto (Optional). */
    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        return repository.findBySettingKey(key).map(AppSettingEntity::getSettingValue);
    }

    /** Retorna valor como String, com default. */
    @Transactional(readOnly = true)
    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /** Alias conveniente para quem chama get(key, defaultValue). */
    @Transactional(readOnly = true)
    public String get(String key, String defaultValue) {
        return getOrDefault(key, defaultValue);
    }

    // ========= LEITURA TIPADA =========

    @Transactional(readOnly = true)
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(v -> {
            var s = v.trim().toLowerCase();
            return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
        }).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        return get(key).map(v -> {
            try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return defaultValue; }
        }).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public long getLong(String key, long defaultValue) {
        return get(key).map(v -> {
            try { return Long.parseLong(v.trim()); } catch (NumberFormatException e) { return defaultValue; }
        }).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return get(key).map(v -> {
            try { return new BigDecimal(v.trim()); } catch (Exception e) { return defaultValue; }
        }).orElse(defaultValue);
    }
}
