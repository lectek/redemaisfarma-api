package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.application.service.AuthService;
import br.com.redemaisfarma.domain.user.Usuario;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepositoryPort authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            @Qualifier("authRepositoryAdapter") AuthRepositoryPort authRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isBlocked(String identifier) {
        return authRepository.isBlocked(identifier);
    }

    @Override
    public void registerFailedAttempt(String identifier) {
        authRepository.registerFailedAttempt(identifier);
    }

    @Override
    public AuthResponse authenticate(String identifier, String rawPassword) {
        Usuario usuario = authRepository.authenticate(identifier, rawPassword);
        String access = UUID.randomUUID().toString();
        String refresh = UUID.randomUUID().toString();
        return new AuthResponse(
                access,
                refresh,
                usuario.getId(),
                usuario.getEmail(),
                toRoleNames(usuario.getRoles()),
                LocalDateTime.now().plusHours(1),
                "rede-mais-farma",
                UUID.randomUUID(),
                LocalDateTime.now()
        );
    }

    @Override
    public boolean isClienteVip(String identifier) {
        return authRepository.isClienteVip(identifier);
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
        final String identifier = request.getUsuario().trim();

        if (isBlocked(identifier)) {
            throw new InvalidCredentialsException("Usuário temporariamente bloqueado por tentativas inválidas.");
        }

        final Optional<Usuario> optUser = authRepository.findByIdentifier(identifier);
        if (optUser.isEmpty()) {
            registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        final Usuario user = optUser.get();
        if (user.getSenha() == null || !passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        authRepository.resetFailedAttempts(identifier);
        authRepository.updateLastAccess(user.getId());

        final String access = UUID.randomUUID().toString();
        final String refresh = UUID.randomUUID().toString();
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime expiresAt = now.plusHours(Boolean.TRUE.equals(request.getLembrarMe()) ? 8 : 1);

        final LoginResponseDTO dto = new LoginResponseDTO();
        dto.setAccessToken(access);
        dto.setRefreshToken(refresh);
        dto.setUserType(resolveUserType(user.getRoles()));
        dto.setUserId(user.getId());
        dto.setFullName(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setPermissions(toRoleNames(user.getRoles()));
        dto.setExpiresAt(expiresAt);
        dto.setLastLoginAt(now);
        dto.setAccountStatus(LoginResponseDTO.AccountStatus.ACTIVE);
        dto.setWelcomeMessage(buildWelcome(user));
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
        return "Bem-vindo à RedeMaisFarma, " + (u.getNome() != null ? u.getNome() : "usuário") + "!";
    }

    private List<String> toRoleNames(Set<String> roles) {
        if (roles == null || roles.isEmpty()) return List.of("ROLE_USER");
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(Collectors.toList());
    }
}
