/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO
 *  br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.img;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import org.springframework.stereotype.Component;

@Component
public class ImageStudioServiceAdapter
implements ImageStudioUseCase {
    public String generateSync(ImageGenRequestDTO req) {
        String key = req.vars() != null ? String.valueOf(req.vars().getOrDefault("codigo", "produto")) : "produto";
        return "http://localhost:8080/assets/autogen/" + req.preset() + "-" + key + ".png";
    }
}

