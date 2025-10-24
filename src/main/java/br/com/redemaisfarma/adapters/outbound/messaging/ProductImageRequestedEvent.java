/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.messaging;

public record ProductImageRequestedEvent(Long productId, String nome, String marca, String categoria, String slug) {
}

