package dev.controle.escala.controller;

import dev.controle.escala.model.Funcionario;
import dev.controle.escala.service.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {
    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping
    public ResponseEntity<List<Funcionario>> listarTodos() {
        return ResponseEntity.ok(funcionarioService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionarioService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Funcionario> salvar(@RequestBody Funcionario f) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioService.salvar(f));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Funcionario> atualizar(@PathVariable Integer id, @RequestBody Funcionario f) {
        return ResponseEntity.ok(funcionarioService.atualizar(id, f));
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Map<String, String>> desativar(@PathVariable Integer id) {
        funcionarioService.desativar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Funcionário desativado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADM_SUPREMO')")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Integer id) {
        funcionarioService.deletar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Funcionário removido com sucesso"));
    }
}