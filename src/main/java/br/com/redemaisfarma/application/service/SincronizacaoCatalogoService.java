// src/main/java/br/com/redemaisfarma/application/service/SincronizacaoCatalogoService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacaoCatalogoService {

    private static final int CHUNK_SIZE = 1000;

    private final ProdutoLegacyRepository legacyRepo;
    private final ProdutoJpaRepository produtoRepo;

    // 👉 garante que este EM é o do MySQL
    @PersistenceContext(unitName = "mysqlPU")
    private EntityManager em;

    /**
     * Lê todos os produtos do Firebird (stream) e faz upsert no MySQL em lotes.
     * Processa em chunks para reduzir SELECTs e manter uso de memória/tempo de transação sob controle.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // evitamos uma transação gigante no MySQL
    public ResumoSync sincronizarTudo() {
        AtomicLong lidos = new AtomicLong();
        AtomicLong inseridos = new AtomicLong();
        AtomicLong atualizados = new AtomicLong();
        AtomicLong ignorados = new AtomicLong();
        AtomicLong erros = new AtomicLong();

        List<ProdutoLegacyEntity> buffer = new ArrayList<>(CHUNK_SIZE);

        try (Stream<ProdutoLegacyEntity> stream = legacyRepo.streamAll()) {
            Iterator<ProdutoLegacyEntity> it = stream.iterator();
            while (it.hasNext()) {
                buffer.add(it.next());

                if (buffer.size() >= CHUNK_SIZE) {
                    processarLote(buffer, lidos, inseridos, atualizados, ignorados, erros);
                    buffer.clear();
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao streamar produtos do legado: {}", e.getMessage(), e);
        }

        // último lote
        if (!buffer.isEmpty()) {
            processarLote(buffer, lidos, inseridos, atualizados, ignorados, erros);
        }

        log.info("Sincronização finalizada: lidos={}, inseridos={}, atualizados={}, ignorados={}, erros={}",
                lidos.get(), inseridos.get(), atualizados.get(), ignorados.get(), erros.get());

        return new ResumoSync(lidos.get(), inseridos.get(), atualizados.get(), ignorados.get(), erros.get());
    }

    /**
     * Cada lote roda numa transação própria para permitir commit incremental e JDBC batching.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mysqlTransactionManager")
    void processarLote(List<ProdutoLegacyEntity> chunk,
                       AtomicLong lidos, AtomicLong inseridos, AtomicLong atualizados, AtomicLong ignorados, AtomicLong erros) {

        // 1) Coleta de chaves únicas do lote
        Set<Long> legacyIds = chunk.stream()
                .map(ProdutoLegacyEntity::getId)
                .filter(Objects::nonNull)
                .map(Number::longValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> codigos = chunk.stream()
                .map(ProdutoLegacyEntity::getCodigoBarras)
                .map(SincronizacaoCatalogoService::normalizaCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 2) Pré-busca dos existentes (no máximo 2 SELECTs p/ lote)
        Map<Long, ProdutoEntity> porLegacy = legacyIds.isEmpty() ? Map.of() :
                produtoRepo.findByLegacyIdIn(legacyIds).stream()
                        .filter(p -> p.getLegacyId() != null)
                        .collect(Collectors.toMap(ProdutoEntity::getLegacyId, p -> p, (a, b) -> a));

        Map<String, ProdutoEntity> porCodigo = codigos.isEmpty() ? Map.of() :
                produtoRepo.findByCodigoBarrasIn(codigos).stream()
                        .filter(p -> p.getCodigoBarras() != null && !p.getCodigoBarras().isBlank())
                        .collect(Collectors.toMap(ProdutoEntity::getCodigoBarras, p -> p, (a, b) -> a));

        List<ProdutoEntity> toSave = new ArrayList<>();

        // 3) Upsert de cada item do lote com idempotência por hash
        for (ProdutoLegacyEntity legacy : chunk) {
            lidos.incrementAndGet();
            try {
                String hash = hashDoLegacy(legacy);
                Long lid = legacy.getId() != null ? legacy.getId().longValue() : null;
                String ean = normalizaCodigo(legacy.getCodigoBarras());

                // observar conflito potencial: legacyId e EAN diferentes apontando p/ entidades distintas
                if (lid != null && ean != null) {
                    ProdutoEntity a = porLegacy.get(lid);
                    ProdutoEntity b = porCodigo.get(ean);
                    if (a != null && b != null && !a.getId().equals(b.getId())) {
                        log.warn("Conflito de chaves: legacyId {} e EAN {} mapeiam entidades diferentes ({} vs {}).",
                                lid, ean, a.getId(), b.getId());
                        // política: prioriza legacyId; o mapeamento abaixo vai consolidar dados em 'a'
                    }
                }

                ProdutoEntity alvo = (lid != null ? porLegacy.get(lid) : null);
                if (alvo == null && ean != null) {
                    alvo = porCodigo.get(ean);
                }

                if (alvo == null) {
                    // INSERT
                    ProdutoEntity novo = ProdutoMapper.fromLegacy(legacy);
                    novo.setLegacyId(lid);
                    novo.setCodigoBarras(ean);
                    novo.setHashLegado(hash);
                    novo.setStatusSync("SINCRONIZADO");
                    toSave.add(novo);
                    inseridos.incrementAndGet();
                } else if (hash.equals(alvo.getHashLegado())) {
                    // IDEMPOTÊNCIA
                    ignorados.incrementAndGet();
                } else {
                    // UPDATE (preserva campos não vindos do legado)
                    ProdutoEntity dados = ProdutoMapper.fromLegacy(legacy);
                    dados.setId(alvo.getId());
                    dados.setLegacyId(lid);
                    dados.setCodigoBarras(ean);

                    if (alvo.getImagem() != null && (dados.getImagem() == null || dados.getImagem().isBlank())) {
                        dados.setImagem(alvo.getImagem());
                    }

                    dados.setHashLegado(hash);
                    dados.setStatusSync("SINCRONIZADO");
                    toSave.add(dados);
                    atualizados.incrementAndGet();
                }

                if (lidos.get() % 1000 == 0) {
                    log.info("Sincronização em andamento... lidos={}, inseridos={}, atualizados={}, ignorados={}",
                            lidos.get(), inseridos.get(), atualizados.get(), ignorados.get());
                }
            } catch (Exception ex) {
                erros.incrementAndGet();
                log.warn("Falha ao processar produto legacyId={}: {}", legacy.getId(), ex.getMessage(), ex);
            }
        }

        // 4) Persistência em batch + limpeza do contexto para evitar vazamento de memória
        if (!toSave.isEmpty()) {
            produtoRepo.saveAll(toSave);
            produtoRepo.flush();
            em.clear();
        }
    }

    /** Gera um hash idempotente das colunas relevantes do legado. */
    private static String hashDoLegacy(ProdutoLegacyEntity p) {
        String base = (p.getId() == null ? "" : p.getId()) + "|" +
                      n(p.getCodigoBarras()) + "|" +
                      n(p.getNome()) + "|" +
                      n(p.getApresentacao()) + "|" +
                      n(p.getPrecoVenda()) + "|" +
                      n(p.getPrecoPromocao()) + "|" +
                      n(p.getSaldo()) + "|" +
                      n(p.getInicioPromocao()) + "|" +
                      n(p.getTerminoPromocao());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(base.hashCode()); // fallback extremo
        }
    }

    private static String n(Object o) { return o == null ? "" : o.toString(); }

    private static String normalizaCodigo(String ean) {
        if (ean == null) return null;
        String v = ean.trim().replaceAll("\\s+", "");
        return v.isEmpty() ? null : v;
    }

    // DTO do resumo
    public record ResumoSync(long lidos, long inseridos, long atualizados, long ignorados, long erros) {}
}
