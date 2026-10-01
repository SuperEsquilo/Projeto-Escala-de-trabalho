package dev.controle.escala.controller;

import dev.controle.escala.dto.EscalaResponseDTO;
import dev.controle.escala.model.Escala;
import dev.controle.escala.service.EscalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/escalas")
public class EscalaController {
    @Autowired
    private EscalaService escalaService;

    @GetMapping
    public ResponseEntity<List<EscalaResponseDTO>> listarTodas(
            @RequestParam(required = false) String data) {
        List<EscalaResponseDTO> lista = (data != null && !data.isBlank())
                ? escalaService.listarPorData(data)
                : escalaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscalaResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(escalaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<EscalaResponseDTO> salvar(@RequestBody Escala escala) {
        return ResponseEntity.status(HttpStatus.CREATED).body(escalaService.salvar(escala));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<EscalaResponseDTO> atualizar(
            @PathVariable Integer id, @RequestBody Escala escala) {
        return ResponseEntity.ok(escalaService.atualizar(id, escala));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADM_SUPREMO')")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Integer id) {
        escalaService.deletar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Escala removida com sucesso"));
    }
}