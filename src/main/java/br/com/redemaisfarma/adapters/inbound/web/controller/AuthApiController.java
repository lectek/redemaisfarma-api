/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.core.AuthenticationException
 *  org.springframework.util.StringUtils
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.ResponseStatus
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.request.RegisterRequest;
import br.com.redemaisfarma.application.core.account.UserAccountService;
import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import br.com.redemaisfarma.application.service.AuthService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"})
public class AuthApiController {
    private final AuthService authService;
    private final UserAccountService accounts;

    public AuthApiController(AuthService authService, UserAccountService accounts) {
        this.authService = authService;
        this.accounts = accounts;
    }

    @PostMapping(path={"/login"}, consumes={"application/json"}, produces={"application/json"})
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            String usuario = AuthApiController.safeTrim(req.getUsuario());
            String senha = AuthApiController.safeTrim(req.getSenha());
            if (!StringUtils.hasText((String)usuario) || !StringUtils.hasText((String)senha)) {
                return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Credenciais inv\u00e1lidas."));
            }
            AuthResponse auth = this.authService.authenticate(usuario, senha);
            LoginResponseDTO.UserType userType = AuthApiController.mapUserType(auth.getRoles());
            LoginResponseDTO dto = new LoginResponseDTO(auth.getAccessToken(), auth.getRefreshToken(), userType, auth.getUserId(), auth.getUsername(), auth.getEmail(), AuthApiController.safeList(auth.getRoles()), auth.getExpiresAt(), LocalDateTime.now(), LoginResponseDTO.AccountStatus.ACTIVE, "Bem-vindo!", auth.getTenantId() != null ? auth.getTenantId() : "rede-mais-farma", auth.getTraceId() != null ? auth.getTraceId() : UUID.randomUUID());
            return ResponseEntity.ok((Object)dto);
        }
        catch (AuthenticationException ex) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Usu\u00e1rio ou senha inv\u00e1lidos."));
        }
        catch (Exception ex) {
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Erro interno ao autenticar."));
        }
    }

    @PostMapping(path={"/register"}, consumes={"application/json"}, produces={"application/json"})
    @ResponseStatus(value=HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        this.accounts.register(req.name(), req.email(), req.cpf(), req.password());
        return this.authService.authenticate(req.email(), req.password());
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static List<String> safeList(List<String> roles) {
        return roles == null ? Collections.emptyList() : roles;
    }

    private static LoginResponseDTO.UserType mapUserType(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return LoginResponseDTO.UserType.CLIENTE;
        }
        for (String r : roles) {
            if (r == null) continue;
            String role = r.toUpperCase(Locale.ROOT);
            if (role.contains("ROLE_ADMIN")) {
                return LoginResponseDTO.UserType.ADMIN;
            }
            if (!role.contains("ROLE_ATENDENTE")) continue;
            return LoginResponseDTO.UserType.ATENDENTE;
        }
        return LoginResponseDTO.UserType.CLIENTE;
    }
}

