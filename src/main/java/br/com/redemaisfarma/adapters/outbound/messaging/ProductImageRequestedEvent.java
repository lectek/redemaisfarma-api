package br.com.redemaisfarma.adapters.outbound.messaging;

/** Evento publicado quando um produto precisa de imagem gerada. */
public record ProductImageRequestedEvent(
        Long productId,
        String nome,        // opcional (pode vir vazio)
        String marca,       // opcional
        String categoria,   // opcional
        String slug         // opcional
) {}
