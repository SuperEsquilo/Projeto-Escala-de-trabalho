package dev.controle.escala.service;

import dev.controle.escala.model.Turno;
import dev.controle.escala.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TurnoService {
    @Autowired
    private TurnoRepository turnoRepository;

    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }

    public Turno buscarPorId(Integer id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turno não encontrado com id: " + id));
    }

    @Transactional
    public Turno salvar(Turno t) {
        validar(t);
        // Calcula duração no servidor — não confia no valor enviado pelo cliente
        t.setDuracaoHoras(calcularDuracao(t.getHoraInicio(), t.getHoraFim()));
        return turnoRepository.save(t);
    }

    @Transactional
    public Turno atualizar(Integer id, Turno t) {
        Turno existente = buscarPorId(id);
        validar(t);
        existente.setNome(t.getNome());
        existente.setHoraInicio(t.getHoraInicio());
        existente.setHoraFim(t.getHoraFim());
        existente.setDuracaoHoras(calcularDuracao(t.getHoraInicio(), t.getHoraFim()));
        existente.setDescricao(t.getDescricao());
        return turnoRepository.save(existente);
    }

    @Transactional
    public void deletar(Integer id) {
        buscarPorId(id); // garante que existe
        turnoRepository.deleteById(id);
    }

    private void validar(Turno t) {
        if (t.getNome() == null || t.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do turno é obrigatório");
        }
        if (t.getHoraInicio() == null || t.getHoraInicio().isBlank()) {
            throw new IllegalArgumentException("Hora de início é obrigatória");
        }
        if (t.getHoraFim() == null || t.getHoraFim().isBlank()) {
            throw new IllegalArgumentException("Hora de fim é obrigatória");
        }
        LocalTime inicio = LocalTime.parse(t.getHoraInicio());
        LocalTime fim = LocalTime.parse(t.getHoraFim());
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Hora de fim deve ser posterior à hora de início");
        }
    }

    private double calcularDuracao(String horaInicio, String horaFim) {
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fim = LocalTime.parse(horaFim);
        long minutos = ChronoUnit.MINUTES.between(inicio, fim);
        return Math.round(minutos / 60.0 * 10.0) / 10.0;
    }
}