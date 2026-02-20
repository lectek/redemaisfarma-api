package br.com.redemaisfarma.application.core.account;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserAccountService service;

    @BeforeEach
    void setUp() {
        when(encoder.encode(anyString())).thenReturn("encoded");
    }

    @Test
    void registerUsesEmailAndGeneratesDefaults() {
        when(usuarios.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarios.existsByCpf(anyString())).thenReturn(false);
        when(usuarios.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.register(null, "User@Example.com", null, "senhaSegura123");

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarios).save(captor.capture());
        UsuarioEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getNome()).isEqualTo("user");
        assertThat(saved.getCpf()).hasSize(11);
        assertThat(saved.getRoles()).isEqualTo(Set.of(Role.of("ROLE_USER")));
    }
}
