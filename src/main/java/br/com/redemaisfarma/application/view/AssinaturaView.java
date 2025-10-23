package br.com.redemaisfarma.application.view;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Projeção para telas/admin (não expõe domínio completo). */
public record AssinaturaView(
        Long id,
        String clienteNome,
        String plano,
        BigDecimal valor,
        LocalDate renovaEm,
        String status
) {}
