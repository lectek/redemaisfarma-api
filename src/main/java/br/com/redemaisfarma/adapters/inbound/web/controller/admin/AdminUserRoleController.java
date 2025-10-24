/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.user.RoleAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/admin/users"})
public class AdminUserRoleController {
    private final RoleAdminService service;

    public AdminUserRoleController(RoleAdminService service) {
        this.service = service;
    }

    @PostMapping(value={"/{userId}/grant-admin"})
    @PreAuthorize(value="hasRole('DEVELOPER')")
    public ResponseEntity<?> grantAdmin(@PathVariable Long userId, HttpServletRequest req) {
        this.service.grantAdmin(userId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value={"/{userId}/revoke-admin"})
    @PreAuthorize(value="hasRole('DEVELOPER')")
    public ResponseEntity<?> revokeAdmin(@PathVariable Long userId, HttpServletRequest req) {
        this.service.revokeAdmin(userId, req);
        return ResponseEntity.ok().build();
    }
}

