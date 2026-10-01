package dev.controle.escala;

import dev.controle.escala.dto.UsuarioResponseDTO;
import dev.controle.escala.model.Usuario;
import dev.controle.escala.repository.UsuarioRepository;
import dev.controle.escala.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Cadastro público deve sempre atribuir perfil USUARIO mesmo se cliente solicitar ADMIN")
    void deveForcarPerfilUsuarioNoCadastroPublico() {
        Usuario payload = new Usuario();
        payload.setNome("Atacante");
        payload.setEmail("atacante@empresa.com");
        payload.setSenha("123456");
        payload.setPerfil("ADM_SUPREMO"); // Tentativa de auto-promoção

        when(usuarioRepository.existsByEmail("atacante@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$hashedPasswordBCrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(10);
            return u;
        });

        UsuarioResponseDTO response = usuarioService.cadastrar(payload);

        assertNotNull(response);
        assertEquals("USUARIO", response.getPerfil(), "Perfil deve ser estritamente USUARIO");
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail já existente")
    void deveRejeitarEmailDuplicado() {
        Usuario payload = new Usuario();
        payload.setNome("Duplicado");
        payload.setEmail("existente@empresa.com");
        payload.setSenha("123456");

        when(usuarioRepository.existsByEmail("existente@empresa.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cadastrar(payload));
        assertTrue(ex.getMessage().contains("já está cadastrado"));
        verify(usuarioRepository, never()).save(any());
    }
}
