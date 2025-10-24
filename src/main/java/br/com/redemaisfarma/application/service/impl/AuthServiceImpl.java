/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.core.exception.InvalidCredentialsException
 *  br.com.redemaisfarma.application.dto.request.LoginRequest
 *  br.com.redemaisfarma.application.dto.response.AuthResponse
 *  br.com.redemaisfarma.application.dto.response.LoginResponseDTO
 *  br.com.redemaisfarma.application.dto.response.LoginResponseDTO$AccountStatus
 *  br.com.redemaisfarma.application.dto.response.LoginResponseDTO$UserType
 *  br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort
 *  br.com.redemaisfarma.domain.user.Usuario
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.application.service.AuthService;
import br.com.redemaisfarma.domain.user.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl
implements AuthService {
    private final AuthRepositoryPort authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(@Qualifier(value="authRepositoryAdapter") AuthRepositoryPort authRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isBlocked(String identifier) {
        return this.authRepository.isBlocked(identifier);
    }

    @Override
    public void registerFailedAttempt(String identifier) {
        this.authRepository.registerFailedAttempt(identifier);
    }

    @Override
    public AuthResponse authenticate(String identifier, String rawPassword) {
        Usuario usuario = this.authRepository.authenticate(identifier, rawPassword);
        String fakeAccessToken = UUID.randomUUID().toString();
        String fakeRefreshToken = UUID.randomUUID().toString();
        return new AuthResponse(fakeAccessToken, fakeRefreshToken, usuario.getId(), usuario.getEmail(), this.toRoleNames(usuario.getRoles()), LocalDateTime.now().plusHours(1L), "rede-mais-farma", UUID.randomUUID(), LocalDateTime.now());
    }

    @Override
    public boolean isClienteVip(String identifier) {
        return this.authRepository.isClienteVip(identifier);
    }

    @Override
    public String getIdentificador(Usuario usuario) {
        return usuario.getEmail();
    }

    @Override
    public String getPassword(Usuario usuario) {
        return usuario.getSenha();
    }

    @Override
    public String getEmail(Usuario usuario) {
        return usuario.getEmail();
    }

    @Override
    public LoginResponseDTO login(LoginRequest request) {
        String identifier = request.getUsuario().trim();
        if (this.isBlocked(identifier)) {
            throw new InvalidCredentialsException("Usu\u00e1rio temporariamente bloqueado por tentativas inv\u00e1lidas.");
        }
        Optional optUser = this.authRepository.findByIdentifier(identifier);
        if (optUser.isEmpty()) {
            this.registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inv\u00e1lidas.");
        }
        Usuario user = (Usuario)optUser.get();
        if (user.getSenha() == null || !this.passwordEncoder.matches((CharSequence)request.getSenha(), user.getSenha())) {
            this.registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inv\u00e1lidas.");
        }
        this.authRepository.resetFailedAttempts(identifier);
        this.authRepository.updateLastAccess(user.getId());
        String access = UUID.randomUUID().toString();
        String refresh = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(Boolean.TRUE.equals(request.getLembrarMe()) ? 8L : 1L);
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setAccessToken(access);
        dto.setRefreshToken(refresh);
        dto.setUserType(this.resolveUserType(user.getRoles()));
        dto.setUserId(user.getId());
        dto.setFullName(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setPermissions(this.toRoleNames(user.getRoles()));
        dto.setExpiresAt(expiresAt);
        dto.setLastLoginAt(now);
        dto.setAccountStatus(LoginResponseDTO.AccountStatus.ACTIVE);
        dto.setWelcomeMessage(this.buildWelcome(user));
        dto.setTenantId(request.getTenantId());
        dto.setTraceId(UUID.randomUUID());
        return dto;
    }

    private LoginResponseDTO.UserType resolveUserType(Set<String> roles) {
        if (roles != null) {
            if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ADMIN"))) {
                return LoginResponseDTO.UserType.ADMIN;
            }
            if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ATENDENTE") || r.equalsIgnoreCase("ATENDENTE"))) {
                return LoginResponseDTO.UserType.ATENDENTE;
            }
        }
        return LoginResponseDTO.UserType.CLIENTE;
    }

    private String buildWelcome(Usuario u) {
        return "Bem-vindo \u00e0 RedeMaisFarma, " + (u.getNome() != null ? u.getNome() : "usu\u00e1rio") + "!";
    }

    private List<String> toRoleNames(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of("ROLE_USER");
        }
        return roles.stream().map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r).collect(Collectors.toList());
    }
}

