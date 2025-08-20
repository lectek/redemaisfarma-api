package br.com.redemaisfarma.adapters.outbound.auth.jwt.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Fornece a hora atual usada para operações com JWT. Pode ser mockado em testes para validar tokens em horários
 * específicos.
 */
public class JwtClock {

    private Clock clock;

    public JwtClock() {
        this.clock = Clock.systemDefaultZone();
    }

    public JwtClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Obtém o tempo atual como {@link Instant}.
     */
    public Instant now() {
        return Instant.now(clock);
    }

    /**
     * Define um clock customizado (útil para testes).
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Restaura para o clock do sistema.
     */
    public void resetToSystemDefault() {
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Retorna o fuso horário atual.
     */
    public ZoneId getZone() {
        return clock.getZone();
    }
}
