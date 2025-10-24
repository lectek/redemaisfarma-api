/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.GetMapping
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShortcutsController {
    @GetMapping(value={"/admin"})
    public String admin() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping(value={"/cliente"})
    public String cliente() {
        return "redirect:/area-cliente";
    }
}

