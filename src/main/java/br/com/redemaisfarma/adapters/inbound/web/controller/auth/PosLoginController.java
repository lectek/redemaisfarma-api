/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.Cookie
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.security.core.Authentication
 *  org.springframework.stereotype.Controller
 *  org.springframework.util.StringUtils
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.server.ResponseStatusException
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PosLoginController {
    private static final String CTX_COOKIE = "APP_CTX";
    private static final int CTX_AGE = 43200;

    @GetMapping(value={"/pos-login"})
    public String posLogin(Authentication auth, HttpServletRequest req, @RequestParam(value="next", required=false) String next) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:" + PosLoginController.withCtx(req, "/login");
        }
        boolean dev = PosLoginController.has(auth, "ROLE_DEVELOPER");
        boolean admin = PosLoginController.has(auth, "ROLE_ADMIN");
        boolean cliente = PosLoginController.has(auth, "ROLE_CLIENTE");
        String safeNext = PosLoginController.sanitizeRelative(next);
        if (safeNext != null) {
            return "redirect:" + PosLoginController.withCtx(req, safeNext);
        }
        String pref = PosLoginController.readCookie(req, CTX_COOKIE);
        if (pref != null) {
            if ("admin".equalsIgnoreCase(pref) && (dev || admin)) {
                return "redirect:" + PosLoginController.withCtx(req, "/admin");
            }
            if ("cliente".equalsIgnoreCase(pref) && cliente) {
                return "redirect:" + PosLoginController.withCtx(req, "/cliente");
            }
        }
        if (dev || admin) {
            return "pages/auth/escolhe-perfil";
        }
        if (cliente) {
            return "redirect:" + PosLoginController.withCtx(req, "/cliente");
        }
        return "redirect:" + PosLoginController.withCtx(req, "/");
    }

    @GetMapping(value={"/entrar-com"})
    public String entrarComo(@RequestParam(value="as") String as, @RequestParam(value="lembrar", required=false) Boolean lembrar, @RequestParam(value="next", required=false) String next, Authentication auth, HttpServletRequest req, HttpServletResponse resp) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:" + PosLoginController.withCtx(req, "/login");
        }
        boolean dev = PosLoginController.has(auth, "ROLE_DEVELOPER");
        boolean admin = PosLoginController.has(auth, "ROLE_ADMIN");
        boolean cliente = PosLoginController.has(auth, "ROLE_CLIENTE");
        String dest = switch (as = as == null ? "" : as.trim().toLowerCase()) {
            case "admin" -> {
                if (!dev && !admin) {
                    throw new ResponseStatusException((HttpStatusCode)HttpStatus.FORBIDDEN, "Sem acesso de administrador");
                }
                yield "/admin";
            }
            case "cliente" -> {
                if (!cliente) {
                    throw new ResponseStatusException((HttpStatusCode)HttpStatus.FORBIDDEN, "Sem acesso de cliente");
                }
                yield "/cliente";
            }
            default -> throw new ResponseStatusException((HttpStatusCode)HttpStatus.BAD_REQUEST, "Op\u00e7\u00e3o inv\u00e1lida");
        };
        String safeNext = PosLoginController.sanitizeRelative(next);
        if (safeNext != null) {
            dest = safeNext;
        }
        if (Boolean.TRUE.equals(lembrar)) {
            PosLoginController.setCookie(resp, CTX_COOKIE, as, 43200, "/", req.isSecure(), false, "Lax");
        }
        return "redirect:" + PosLoginController.withCtx(req, dest);
    }

    @GetMapping(value={"/limpar-escolha"})
    public String limparEscolha(HttpServletRequest req, HttpServletResponse resp) {
        PosLoginController.setCookie(resp, CTX_COOKIE, "", 0, "/", req.isSecure(), false, "Lax");
        return "redirect:" + PosLoginController.withCtx(req, "/pos-login");
    }

    private static boolean has(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) {
            return null;
        }
        return Arrays.stream(req.getCookies()).filter(c -> name.equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }

    private static String sanitizeRelative(String next) {
        if (!StringUtils.hasText((String)next)) {
            return null;
        }
        if ((next = next.trim()).startsWith("http://") || next.startsWith("https://")) {
            return null;
        }
        if (next.startsWith("//")) {
            return null;
        }
        try {
            URI u = URI.create(next);
            if (u.isAbsolute()) {
                return null;
            }
            String p = u.getPath();
            if (!StringUtils.hasText((String)p)) {
                return null;
            }
            return p.startsWith("/") ? next : "/" + next;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static String withCtx(HttpServletRequest req, String path) {
        String ctx = req.getContextPath();
        if (!StringUtils.hasText((String)ctx) || "/".equals(ctx)) {
            return path;
        }
        return path.startsWith(ctx) ? path : ctx + path;
    }

    private static void setCookie(HttpServletResponse resp, String name, String value, int maxAge, String path, boolean secure, boolean httpOnly, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append('=').append(value == null ? "" : value).append("; Path=").append(path == null ? "/" : path);
        if (maxAge >= 0) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (secure) {
            sb.append("; Secure");
        }
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        if (StringUtils.hasText((String)sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }
        resp.addHeader("Set-Cookie", sb.toString());
    }
}

