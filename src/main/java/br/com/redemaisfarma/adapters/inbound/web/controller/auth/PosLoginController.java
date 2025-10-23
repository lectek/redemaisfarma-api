// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/auth/PosLoginController.java
package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Arrays;

@Controller
public class PosLoginController {

  private static final String CTX_COOKIE = "APP_CTX";
  private static final int CTX_AGE = 60 * 60 * 12; // 12h

  /** Tela/roteamento pós-login. Aceita ?next=/rota (relativa). */
  @GetMapping("/pos-login")
  public String posLogin(Authentication auth,
                         HttpServletRequest req,
                         @RequestParam(value = "next", required = false) String next) {

    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:" + withCtx(req, "/login");
    }

    boolean dev     = has(auth, "ROLE_DEVELOPER");
    boolean admin   = has(auth, "ROLE_ADMIN");
    boolean cliente = has(auth, "ROLE_CLIENTE");

    // 1) Deep link seguro
    String safeNext = sanitizeRelative(next);
    if (safeNext != null) {
      return "redirect:" + withCtx(req, safeNext);
    }

    // 2) Preferência salva (cookie) validando papéis
    String pref = readCookie(req, CTX_COOKIE);
    if (pref != null) {
      if ("admin".equalsIgnoreCase(pref) && (dev || admin)) return "redirect:" + withCtx(req, "/admin");
      if ("cliente".equalsIgnoreCase(pref) && cliente)      return "redirect:" + withCtx(req, "/cliente");
    }

    // 3) DEV/Admin veem a tela de escolha
    if (dev || admin) {
      return "pages/auth/escolhe-perfil"; // 👈 nome da view existente
    }

    // 4) Cliente puro
    if (cliente) {
      return "redirect:" + withCtx(req, "/cliente");
    }

    // 5) Fallback (autenticado sem perfil conhecido)
    return "redirect:" + withCtx(req, "/");
  }

  /** Endpoint que fixa o contexto e redireciona. Aceita ?as=admin|cliente&lembrar=true&next=/rota */
  @GetMapping("/entrar-com")
  public String entrarComo(@RequestParam("as") String as,
                           @RequestParam(value = "lembrar", required = false) Boolean lembrar,
                           @RequestParam(value = "next", required = false) String next,
                           Authentication auth,
                           HttpServletRequest req,
                           HttpServletResponse resp) {

    if (auth == null || !auth.isAuthenticated()) {
      return "redirect:" + withCtx(req, "/login");
    }

    boolean dev     = has(auth, "ROLE_DEVELOPER");
    boolean admin   = has(auth, "ROLE_ADMIN");
    boolean cliente = has(auth, "ROLE_CLIENTE");

    String dest;
    as = as == null ? "" : as.trim().toLowerCase();
    switch (as) {
      case "admin" -> {
        if (!(dev || admin)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso de administrador");
        dest = "/admin";
      }
      case "cliente" -> {
        if (!cliente) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso de cliente");
        dest = "/cliente";
      }
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opção inválida");
    }

    // Deep-link opcional (relativo e seguro)
    String safeNext = sanitizeRelative(next);
    if (safeNext != null) dest = safeNext;

    // Grava preferência (opcional, cookie não sensível)
    if (Boolean.TRUE.equals(lembrar)) {
      setCookie(resp, CTX_COOKIE, as, CTX_AGE, "/", req.isSecure(), /*httpOnly*/ false, "Lax");
    }

    return "redirect:" + withCtx(req, dest);
  }

  @GetMapping("/limpar-escolha")
  public String limparEscolha(HttpServletRequest req, HttpServletResponse resp) {
    setCookie(resp, CTX_COOKIE, "", 0, "/", req.isSecure(), /*httpOnly*/ false, "Lax"); // apaga
    return "redirect:" + withCtx(req, "/pos-login");
  }

  // ===== helpers =====

  private static boolean has(Authentication auth, String role) {
    return auth != null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
  }

  private static String readCookie(HttpServletRequest req, String name) {
    if (req.getCookies() == null) return null;
    return Arrays.stream(req.getCookies())
        .filter(c -> name.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  /** Garante rota relativa (evita open redirect). Retorna null se inválida. */
  private static String sanitizeRelative(String next) {
    if (!StringUtils.hasText(next)) return null;
    next = next.trim();
    if (next.startsWith("http://") || next.startsWith("https://")) return null;
    if (next.startsWith("//")) return null;
    try {
      URI u = URI.create(next);
      if (u.isAbsolute()) return null;
      String p = u.getPath();
      if (!StringUtils.hasText(p)) return null;
      return p.startsWith("/") ? next : ("/" + next); // preserva query/fragment
    } catch (Exception e) {
      return null;
    }
  }

  /** Prefixa o contextPath se houver. */
  private static String withCtx(HttpServletRequest req, String path) {
    String ctx = req.getContextPath();
    if (!StringUtils.hasText(ctx) || "/".equals(ctx)) return path;
    return path.startsWith(ctx) ? path : (ctx + path);
  }

  /** Emite um único Set-Cookie com SameSite/HttpOnly/Secure. */
  private static void setCookie(HttpServletResponse resp, String name, String value,
                                int maxAge, String path, boolean secure, boolean httpOnly, String sameSite) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append('=').append(value == null ? "" : value)
      .append("; Path=").append(path == null ? "/" : path);
    if (maxAge >= 0) sb.append("; Max-Age=").append(maxAge);
    if (secure) sb.append("; Secure");
    if (httpOnly) sb.append("; HttpOnly");
    if (StringUtils.hasText(sameSite)) sb.append("; SameSite=").append(sameSite);
    resp.addHeader("Set-Cookie", sb.toString());
  }
}
