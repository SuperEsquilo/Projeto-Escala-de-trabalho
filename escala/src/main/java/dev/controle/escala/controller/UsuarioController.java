package dev.controle.escala.controller;

import dev.controle.escala.dto.UsuarioResponseDTO;
import dev.controle.escala.model.Usuario;
import dev.controle.escala.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody Usuario dto) {
        UsuarioResponseDTO criado = usuarioService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        UsuarioResponseDTO logado = usuarioService.autenticar(email, senha);
        return ResponseEntity.ok(logado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Usuário removido com sucesso"));
    }
}