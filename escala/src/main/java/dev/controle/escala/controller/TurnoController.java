package dev.controle.escala.controller;

import dev.controle.escala.model.Turno;
import dev.controle.escala.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {
    @Autowired
    private TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<Turno>> listarTodos() {
        return ResponseEntity.ok(turnoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turno> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(turnoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Turno> salvar(@RequestBody Turno t) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.salvar(t));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Turno> atualizar(@PathVariable Integer id, @RequestBody Turno t) {
        return ResponseEntity.ok(turnoService.atualizar(id, t));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Integer id) {
        turnoService.deletar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Turno removido com sucesso"));
    }
}