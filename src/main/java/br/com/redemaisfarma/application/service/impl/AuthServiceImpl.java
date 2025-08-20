package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.application.service.AuthService;
import br.com.redemaisfarma.domain.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepositoryPort authRepository;

    @Autowired
    public AuthServiceImpl(AuthRepositoryPort authRepository) {
        this.authRepository = authRepository;
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
    public AuthResponse authenticate(String identifier, String password) {
        Usuario usuario = authRepository.authenticate(identifier, password);

        // Aqui você pode usar um JWT real futuramente.
        String fakeAccessToken = UUID.randomUUID().toString();
        String fakeRefreshToken = UUID.randomUUID().toString();

        return new AuthResponse(fakeAccessToken, fakeRefreshToken, usuario.getId(), usuario.getEmail(), // username
                List.of("ROLE_USER"), // roles padrão
                LocalDateTime.now().plusHours(1), // expiresAt
                "rede-mais-farma", // tenantId
                UUID.randomUUID(), // traceId
                LocalDateTime.now() // issuedAt
        );
    }

    @Override
    public boolean isClienteVip(String identifier) {
        return authRepository.isClienteVip(identifier);
    }

    @Override
    public String getIdentificador(Usuario usuario) {
        return usuario.getEmail(); // ou getCpf()
    }

    @Override
    public String getPassword(Usuario usuario) {
        return usuario.getSenha(); // supondo que o campo seja "senha"
    }

    @Override
    public String getEmail(Usuario usuario) {
        return usuario.getEmail();
    }
}
