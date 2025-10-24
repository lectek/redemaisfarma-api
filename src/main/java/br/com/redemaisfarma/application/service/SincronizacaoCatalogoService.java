/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.EntityManager
 *  jakarta.persistence.PersistenceContext
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Propagation
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SincronizacaoCatalogoService {
    @Generated
    private static final Logger log;
    private static final int CHUNK_SIZE = 1000;
    private final ProdutoLegacyRepository legacyRepo;
    private final ProdutoJpaRepository produtoRepo;
    @PersistenceContext(unitName="mysqlPU")
    private EntityManager em;

    @Transactional(propagation=Propagation.NOT_SUPPORTED)
    public ResumoSync sincronizarTudo() {
        throw new Error("Unresolved compilation problem: \n");
    }

    @Transactional(propagation=Propagation.REQUIRES_NEW, transactionManager="mysqlTransactionManager")
    void processarLote(List<ProdutoLegacyEntity> list, AtomicLong atomicLong, AtomicLong atomicLong2, AtomicLong atomicLong3, AtomicLong atomicLong4, AtomicLong atomicLong5) {
        throw new Error("Unresolved compilation problems: \n\tType mismatch: cannot convert from Object to Map\n\tThe method getLegacyId() is undefined for the type Object\n\tType mismatch: cannot convert from Object to Map\n\tThe method getCodigoBarras() is undefined for the type Object\n\tThe method getCodigoBarras() is undefined for the type Object\n");
    }

    private static String hashDoLegacy(ProdutoLegacyEntity produtoLegacyEntity) {
        throw new Error("Unresolved compilation problem: \n");
    }

    private static String n(Object object) {
        throw new Error("Unresolved compilation problem: \n");
    }

    private static String normalizaCodigo(String string) {
        throw new Error("Unresolved compilation problem: \n");
    }

    @Generated
    public SincronizacaoCatalogoService(ProdutoLegacyRepository produtoLegacyRepository, ProdutoJpaRepository produtoJpaRepository) {
        throw new Error("Unresolved compilation problem: \n");
    }

    public static record ResumoSync(int lidos, int inseridos, int atualizados, int ignorados, int erros) {}}



