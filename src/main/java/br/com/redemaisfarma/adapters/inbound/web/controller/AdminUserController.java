// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/AdminUserController.java
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.user.UsuarioFormDTO;
import br.com.redemaisfarma.application.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String papel,
                         Model model) {
        model.addAttribute("usuarios", userService.listar(q, papel));
        return "admin/usuarios/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new UsuarioFormDTO(null, "", "", "", "", true));
        return "admin/usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", userService.buscarForm(id));
        return "admin/usuarios/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute @Valid UsuarioFormDTO usuario) {
        userService.salvar(usuario);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}")
    public String perfil(@PathVariable Long id, Model model) {
        model.addAttribute("u", userService.buscarPerfil(id));
        model.addAttribute("logs", java.util.Collections.emptyList()); // placeholder
        return "admin/usuarios/perfil";
    }
}

