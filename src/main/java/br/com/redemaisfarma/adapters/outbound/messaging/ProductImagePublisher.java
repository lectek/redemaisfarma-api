/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.messaging;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;

public interface ProductImagePublisher {
    public void publish(ProductImageRequestedEvent var1);
}

