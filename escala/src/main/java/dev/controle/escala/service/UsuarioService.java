package dev.controle.escala.service;

import dev.controle.escala.dto.UsuarioResponseDTO;
import dev.controle.escala.model.Usuario;
import dev.controle.escala.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO cadastrar(Usuario dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        if (dto.getSenha() == null || dto.getSenha().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail().toLowerCase().trim());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha())); // BCrypt
        usuario.setPerfil("USUARIO"); // perfil fixo — nunca confia no cliente
        Usuario salvo = usuarioRepository.save(usuario);
        return toDTO(salvo);
    }

    public UsuarioResponseDTO autenticar(String email, String senhaPlana) {
        if (email == null || senhaPlana == null) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios");
        }
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos"));
        if (!passwordEncoder.matches(senhaPlana, usuario.getSenha())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }
        return toDTO(usuario);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void deletar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado com id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}