package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.domain.Usuario;

public interface AuthService {

    boolean isBlocked(String identifier);

    void registerFailedAttempt(String identifier);

    AuthResponse authenticate(String identifier, String password);

    boolean isClienteVip(String identifier);

    String getIdentificador(Usuario usuario);

    String getPassword(Usuario usuario);

    String getEmail(Usuario usuario);
}
