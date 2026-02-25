package br.com.redemaisfarma.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
public class EstoqueFisicoCsvService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueFisicoCsvService.class);
    private static final Path CSV_PATH = Paths.get("Estoque Fisico.csv");

    private volatile long cachedLastModified = Long.MIN_VALUE;
    private volatile List<EstoqueItem> cachedItems = List.of();

    public List<EstoqueItem> search(String query) {
        List<EstoqueItem> source = this.loadCached();
        String termo = normalize(query).toLowerCase(Locale.ROOT);
        if (termo.isBlank()) {
            return source;
        }
        return source.stream()
                .filter(item -> item.searchBlob().contains(termo))
                .toList();
    }

    private List<EstoqueItem> loadCached() {
        long lastModified = this.resolveLastModified(CSV_PATH);
        if (lastModified == this.cachedLastModified) {
            return this.cachedItems;
        }
        synchronized (this) {
            long current = this.resolveLastModified(CSV_PATH);
            if (current == this.cachedLastModified) {
                return this.cachedItems;
            }
            List<EstoqueItem> parsed = this.parseCsv(CSV_PATH);
            this.cachedItems = parsed;
            this.cachedLastModified = current;
            log.info("[estoque-csv] cache atualizado: {} itens", parsed.size());
            return parsed;
        }
    }

    private long resolveLastModified(Path path) {
        try {
            if (Files.exists(path)) {
                return Files.getLastModifiedTime(path).toMillis();
            }
        } catch (IOException ex) {
            log.warn("[estoque-csv] nao foi possivel ler lastModified", ex);
        }
        return -1L;
    }

    private List<EstoqueItem> parseCsv(Path path) {
        if (!Files.exists(path)) {
            log.warn("[estoque-csv] arquivo nao encontrado: {}", path.toAbsolutePath());
            return List.of();
        }

        LinkedHashMap<String, EstoqueItem> dedupe = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                EstoqueItem item = this.parseLine(line);
                if (item == null) {
                    continue;
                }
                String key = this.buildKey(item);
                EstoqueItem previous = dedupe.get(key);
                if (previous == null || item.estoque() > previous.estoque()) {
                    dedupe.put(key, item);
                }
            }
        } catch (IOException ex) {
            log.error("[estoque-csv] falha ao ler arquivo {}", path.toAbsolutePath(), ex);
            return List.of();
        }

        return new ArrayList<>(dedupe.values());
    }

    private String buildKey(EstoqueItem item) {
        if (item.legacyId() != null) {
            return "L" + item.legacyId();
        }
        if (!item.codigoBarras().isBlank()) {
            return "B" + item.codigoBarras();
        }
        return "N" + item.nome().toLowerCase(Locale.ROOT);
    }

    private EstoqueItem parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] raw = line.split(";");
        List<String> tokens = new ArrayList<>(raw.length);
        for (String value : raw) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                tokens.add(normalized);
            }
        }
        if (tokens.size() < 7) {
            return null;
        }
        if (isHeaderOrSummary(tokens)) {
            return null;
        }

        String nome = tokens.get(2);
        if (nome.length() < 3) {
            return null;
        }

        Long legacyId = parseLong(tokens.get(1));
        String codigoBarras = normalizeBarcode(tokens.get(0));
        if (legacyId == null && codigoBarras.isBlank()) {
            return null;
        }

        Integer estoque = parseInteger(tokens.get(6));
        if (estoque == null || estoque < 0) {
            estoque = 0;
        }

        String fabricante = tokens.size() > 3 ? tokens.get(3) : "";
        BigDecimal precoTabela = tokens.size() > 7 ? parseMoney(tokens.get(7)) : null;
        BigDecimal precoVenda = tokens.size() > 8 ? parseMoney(tokens.get(8)) : null;

        String searchBlob = (
                nome + " " +
                (legacyId == null ? "" : legacyId) + " " +
                codigoBarras + " " +
                fabricante
        ).toLowerCase(Locale.ROOT);

        return new EstoqueItem(
                legacyId,
                codigoBarras,
                nome,
                fabricante,
                estoque,
                precoTabela,
                precoVenda,
                searchBlob
        );
    }

    private static boolean isHeaderOrSummary(List<String> tokens) {
        String first = tokens.getFirst().toUpperCase(Locale.ROOT);
        String second = tokens.size() > 1 ? tokens.get(1).toUpperCase(Locale.ROOT) : "";
        if (first.startsWith("SUB-TOTAL") || first.startsWith("GERADO POR")) {
            return true;
        }
        if (first.contains("C") && first.contains("BARRAS") && second.contains("C")) {
            return true;
        }
        if (first.contains("PR.PROMO") || first.contains("LOCALIZA")) {
            return true;
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replace('\u00a0', ' ');
    }

    private static Integer parseInteger(String value) {
        String cleaned = normalize(value).replaceAll("[^0-9-]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        String cleaned = normalize(value).replaceAll("[^0-9]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseMoney(String value) {
        String cleaned = normalize(value)
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .replaceAll("[^0-9.-]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String normalizeBarcode(String value) {
        String raw = normalize(value);
        if (raw.isBlank()) {
            return "";
        }

        String cleaned = raw.replace(" ", "");
        if (cleaned.contains("E") || cleaned.contains("e")) {
            try {
                String decimalNotation = cleaned.replace(".", "").replace(",", ".");
                String plain = new BigDecimal(decimalNotation).toPlainString();
                return plain.replaceAll("\\D+", "");
            } catch (Exception ex) {
                // fallback para limpeza simples
            }
        }
        return cleaned.replaceAll("\\D+", "");
    }

    public record EstoqueItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            String fabricante,
            Integer estoque,
            BigDecimal precoTabela,
            BigDecimal precoVenda,
            String searchBlob
    ) {
    }
}
