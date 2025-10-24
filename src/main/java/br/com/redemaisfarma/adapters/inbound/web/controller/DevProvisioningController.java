/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.DevProvisioningService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/dev/provision"})
public class DevProvisioningController {
    private final DevProvisioningService service;

    public DevProvisioningController(DevProvisioningService service) {
        this.service = service;
    }

    @PostMapping(value={"/start"})
    public ResponseEntity<Map<String, Object>> start(@RequestParam @Email String email) {
        String deliveryId = this.service.start(email);
        return ResponseEntity.ok(Map.of("status", "sent", "deliveryId", deliveryId, "destination", email));
    }

    @PostMapping(value={"/verify"})
    public ResponseEntity<Map<String, Object>> verify(@RequestParam(value="token") @NotBlank String token, @RequestParam(value="password", required=false) String password, @RequestParam(value="cpfIfNew", required=false) String cpfIfNew, @RequestParam(value="nomeIfNew", required=false) String nomeIfNew) {
        this.service.verifyByToken(token, password, cpfIfNew, nomeIfNew);
        return ResponseEntity.ok(Map.of("status", "dev-enabled"));
    }
}

