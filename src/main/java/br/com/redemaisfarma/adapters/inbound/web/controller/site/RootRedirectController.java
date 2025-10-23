package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class RootRedirectController {

  /**
   * Sem área pública:
   * - Usuário autenticado → /pos-login (opcionalmente com ?next= relativo)
   * - Anônimo → /auth/login
   */
  @GetMapping("/")
  public String redirectRoot(Authentication auth,
                             HttpServletRequest req,
                             @RequestParam(value = "next", required = false) String next) {

    if (auth != null && auth.isAuthenticated()) {
      String safeNext = sanitizeRelative(next);
      String dest = "/pos-login";
      if (safeNext != null) {
        dest = appendQuery(dest, Map.of("next", safeNext));
      }
      return "redirect:" + withCtx(req, dest);
    }
    return "redirect:" + withCtx(req, "/auth/login");
  }

  private static String sanitizeRelative(String next) {
    if (!StringUtils.hasText(next)) return null;
    next = next.trim();
    if (next.startsWith("http://") || next.startsWith("https://") || next.startsWith("//")) return null;
    try {
      URI u = URI.create(next);
      if (u.isAbsolute()) return null;
      String path = u.getPath();
      if (!StringUtils.hasText(path)) return null;
      String normalized = path.startsWith("/") ? path : ("/" + path);
      String query = u.getQuery();
      String frag  = u.getFragment();
      StringBuilder sb = new StringBuilder(normalized);
      if (StringUtils.hasText(query)) sb.append('?').append(query);
      if (StringUtils.hasText(frag))  sb.append('#').append(frag);
      return sb.toString();
    } catch (Exception e) {
      return null;
    }
  }

  private static String withCtx(HttpServletRequest req, String path) {
    String ctx = req.getContextPath();
    if (!StringUtils.hasText(ctx) || "/".equals(ctx)) return path;
    return path.startsWith(ctx) ? path : (ctx + path);
  }

  private static String appendQuery(String base, Map<String, String> params) {
    if (params == null || params.isEmpty()) return base;
    StringBuilder sb = new StringBuilder(base);
    sb.append(base.contains("?") ? "&" : "?");
    boolean first = true;
    for (Map.Entry<String, String> e : new LinkedHashMap<>(params).entrySet()) {
      if (!first) sb.append('&');
      first = false;
      sb.append(java.net.URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
        .append('=')
        .append(java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
    }
    return sb.toString();
  }
}
