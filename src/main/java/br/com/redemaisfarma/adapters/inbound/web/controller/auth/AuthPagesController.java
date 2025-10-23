package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthPagesController {

  @GetMapping("/login")
  public String loginPage(Model model) {
    if (!model.containsAttribute("loginForm")) {
      model.addAttribute("loginForm", new LoginForm());
    }
    return "pages/auth/login";
  }

  // DTO simples apenas para binding do Thymeleaf
  public static class LoginForm {
    private String usuario;
    private String senha;

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
  }
}
