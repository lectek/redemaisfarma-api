package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacaoCatalogoService {

    private static final int CHUNK_SIZE = 1000;

    private final ProdutoLegacyRepository legacyRepo;
    private final ProdutoRepository produtoRepo;

    @PersistenceContext(unitName = "mysqlPU")
    private EntityManager em;

    /** Lê tudo do legado em páginas e faz upsert no MySQL. */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResumoSync sincronizarTudo() {
        long t0 = System.currentTimeMillis();
        AtomicLong lidos = new AtomicLong();
        AtomicLong inseridos = new AtomicLong();
        AtomicLong atualizados = new AtomicLong();
        AtomicLong ignorados = new AtomicLong();
        AtomicLong erros = new AtomicLong();

        int page = 0;
        while (true) {
            Page<ProdutoLegacyEntity> pagina = legacyRepo.findAll(PageRequest.of(page, CHUNK_SIZE));
            List<ProdutoLegacyEntity> lote = pagina.getContent();
            if (lote.isEmpty()) break;

            try {
                processarLote(lote, lidos, inseridos, atualizados, ignorados, erros);
            } catch (Exception e) {
                log.error("Erro processando lote (page={}, size={}): {}", page, lote.size(), e.getMessage(), e);
                erros.addAndGet(lote.size());
            }

            if (!pagina.hasNext()) break;
            page++;
        }

        long ms = System.currentTimeMillis() - t0;
        log.info("Sync catálogo finalizada em {} ms | lidos={}, inseridos={}, atualizados={}, ignorados={}, erros={}",
                ms, lidos.get(), inseridos.get(), atualizados.get(), ignorados.get(), erros.get());

        return new ResumoSync(
                cap(lidos.get()), cap(inseridos.get()), cap(atualizados.get()), cap(ignorados.get()), cap(erros.get())
        );
    }

    /** Processa um lote dentro de uma transação nova no MySQL. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mysqlTransactionManager")
    void processarLote(
            List<ProdutoLegacyEntity> lote,
            AtomicLong lidos,
            AtomicLong inseridos,
            AtomicLong atualizados,
            AtomicLong ignorados,
            AtomicLong erros
    ) {
        int i = 0;
        for (ProdutoLegacyEntity legacy : lote) {
            lidos.incrementAndGet();
            try {
                String legacyId = legacy.getId() == null ? "" : String.valueOf(legacy.getId());
                String cb = normalizaCodigo(legacy.getCodigoBarras());

                ProdutoEntity destino = null;

                // 1) Tenta por legacyId
                if (!legacyId.isBlank()) {
                    destino = produtoRepo.findByLegacyId(Long.valueOf(legacyId)).orElse(null);
                }

                // 2) Tenta por código de barras
                if (destino == null && !cb.isBlank()) {
                    destino = produtoRepo.findByCodigoBarras(cb).orElse(null);
                }

                String novoHash = hashDoLegacy(legacy);

                if (destino == null) {
                    // INSERT
                    ProdutoEntity novo = new ProdutoEntity();
                    novo.setNome(chooseFirstNonBlank(legacy.getNome(), legacy.getApresentacao()));
                    novo.setDescricao(legacy.getApresentacao());
                    novo.setCodigoBarras(cb);

                    // preços/estoque
                    novo.setPrecoVenda(precoEfetivo(legacy));
                    novo.setPrecoPromocional(legacy.getPrecoPromocao());
                    if (legacy.getSaldo() != null) {
                        novo.setEstoque(legacy.getSaldo().intValue());
                    }
                    if (legacy.getEstoqueMinimo() != null) {
                        novo.setEstoque(Math.max(novo.getEstoque() == null ? 0 : novo.getEstoque(), 0)); // mantém não-negativo
                    }

                    // metadados
                    if (!legacyId.isBlank()) {
                        novo.setLegacyId(Long.valueOf(legacyId));
                    }
                    novo.setHashLegado(novoHash);
                    novo.setDataImportacao(LocalDateTime.now());

                    produtoRepo.save(novo);
                    inseridos.incrementAndGet();
                } else {
                    // UPDATE se mudou
                    String hashAtual = safe(destino.getHashLegado());
                    if (!Objects.equals(hashAtual, novoHash)) {
                        destino.setNome(chooseFirstNonBlank(legacy.getNome(), legacy.getApresentacao()));
                        destino.setDescricao(legacy.getApresentacao());
                        destino.setCodigoBarras(cb);
                        destino.setPrecoVenda(precoEfetivo(legacy));
                        destino.setPrecoPromocional(legacy.getPrecoPromocao());

                        if (legacy.getSaldo() != null) {
                            destino.setEstoque(legacy.getSaldo().intValue());
                        }

                        if (!legacyId.isBlank()) {
                            destino.setLegacyId(Long.valueOf(legacyId));
                        }
                        destino.setHashLegado(novoHash);
                        destino.setDataImportacao(LocalDateTime.now());

                        produtoRepo.save(destino);
                        atualizados.incrementAndGet();
                    } else {
                        ignorados.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                erros.incrementAndGet();
                log.warn("Falha ao sincronizar item do lote (idx={}): {}", i, e.getMessage(), e);
            }

            if (++i % 250 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();
    }

    /* ========================= Helpers ========================= */

    private static boolean isPromocaoAtiva(ProdutoLegacyEntity l) {
        if (l.getPrecoPromocao() == null) return false;
        LocalDateTime now = LocalDateTime.now();
        boolean iniciou = l.getInicioPromocao() == null || !now.isBefore(l.getInicioPromocao());
        boolean naoTerminou = l.getTerminoPromocao() == null || !now.isAfter(l.getTerminoPromocao());
        return iniciou && naoTerminou;
    }

    private static BigDecimal precoEfetivo(ProdutoLegacyEntity l) {
        if (isPromocaoAtiva(l) && l.getPrecoPromocao() != null) return l.getPrecoPromocao();
        if (l.getPrecoVenda() != null) return l.getPrecoVenda();
        return l.getPrecoAnterior() != null ? l.getPrecoAnterior() : BigDecimal.ZERO;
    }

    private static String chooseFirstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return "Sem nome";
    }

    private static String normalizaCodigo(String s) {
        if (s == null) return "";
        String digits = s.replaceAll("\\D+", "");
        return digits.replaceFirst("^0+(?!$)", "");
    }

    private static String hashDoLegacy(ProdutoLegacyEntity l) {
        String base = String.join("|",
                sv(l.getId()),
                sv(l.getNome()),
                normalizaCodigo(l.getCodigoBarras()),
                bd(l.getSaldo()),
                bd(l.getPrecoVenda()),
                bd(l.getPrecoPromocao()),
                bd(l.getEstoqueMinimo()),
                bd(l.getMargemLucro()),
                dt(l.getInicioPromocao()),
                dt(l.getTerminoPromocao()),
                bd(l.getBonus()),
                sv(l.getApresentacao()),
                bd(l.getPrecoAnterior()),
                sv(l.getFornecedorId()),
                sv(l.getCategoriaId()),
                sv(l.getComissaoId())
        );
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(base.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return base;
        }
    }

    private static String sv(Object o) { return o == null ? "" : String.valueOf(o).trim(); }
    private static String bd(BigDecimal b) { return b == null ? "" : b.stripTrailingZeros().toPlainString(); }
    private static String dt(LocalDateTime t) { return t == null ? "" : t.toString(); }
    private static String safe(String s) { return s == null ? "" : s; }

    private static int cap(long v) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, v));
    }

    /** Resumo agregado da sincronização. */
    public record ResumoSync(int lidos, int inseridos, int atualizados, int ignorados, int erros) {}
}
