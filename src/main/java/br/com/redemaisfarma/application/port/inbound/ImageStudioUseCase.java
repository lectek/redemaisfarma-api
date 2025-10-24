/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.port.inbound;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;

public interface ImageStudioUseCase {
    public String generateSync(ImageGenRequestDTO var1);
}

