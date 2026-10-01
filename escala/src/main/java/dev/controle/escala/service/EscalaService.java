package dev.controle.escala.service;

import dev.controle.escala.dto.EscalaResponseDTO;
import dev.controle.escala.model.Escala;
import dev.controle.escala.model.Funcionario;
import dev.controle.escala.model.Turno;
import dev.controle.escala.repository.EscalaRepository;
import dev.controle.escala.repository.FuncionarioRepository;
import dev.controle.escala.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EscalaService {

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    public List<EscalaResponseDTO> listarTodas() {
        return escalaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EscalaResponseDTO> listarPorData(String data) {
        return escalaRepository.findByData(data)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EscalaResponseDTO buscarPorId(Integer id) {
        Escala escala = escalaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Escala não encontrada com id: " + id));
        return toDTO(escala);
    }

    @Transactional
    public EscalaResponseDTO salvar(Escala e) {
        validar(e, null);
        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("AGENDADO");
        }
        Escala salva = escalaRepository.save(e);
        return toDTO(salva);
    }

    @Transactional
    public EscalaResponseDTO atualizar(Integer id, Escala e) {
        Escala existente = escalaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Escala não encontrada com id: " + id));

        validar(e, id);

        existente.setFuncionarioId(e.getFuncionarioId());
        existente.setTurnoId(e.getTurnoId());
        existente.setData(e.getData());
        existente.setStatus(e.getStatus());
        existente.setObservacao(e.getObservacao());

        return toDTO(escalaRepository.save(existente));
    }

    @Transactional
    public void deletar(Integer id) {
        if (!escalaRepository.existsById(id)) {
            throw new IllegalArgumentException("Escala não encontrada com id: " + id);
        }
        escalaRepository.deleteById(id);
    }

    private void validar(Escala e, Integer excludeId) {
        if (e.getFuncionarioId() == null) {
            throw new IllegalArgumentException("Funcionário é obrigatório");
        }
        if (e.getTurnoId() == null) {
            throw new IllegalArgumentException("Turno é obrigatório");
        }
        if (e.getData() == null || e.getData().isBlank()) {
            throw new IllegalArgumentException("Data é obrigatória");
        }

        // Verifica se o funcionário existe e está ativo
        Funcionario funcionario = funcionarioRepository.findById(e.getFuncionarioId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        if (!Boolean.TRUE.equals(funcionario.getAtivo())) {
            throw new IllegalArgumentException("Não é possível escalar um funcionário inativo");
        }

        // Verifica se o turno existe
        if (!turnoRepository.existsById(e.getTurnoId())) {
            throw new IllegalArgumentException("Turno não encontrado");
        }

        // Verifica sobreposição de horário (excludeId == null = novo registro, usa -1 para não excluir nenhum)
        int idParaExcluir = (excludeId != null) ? excludeId : -1;
        if (escalaRepository.existeSobreposicaoHorario(
                e.getFuncionarioId(), e.getTurnoId(), e.getData(), idParaExcluir)) {
            throw new IllegalArgumentException(
                    "O funcionário já possui uma escala com horário sobreposto nesta data");
        }

        // Valida status
        String status = e.getStatus();
        if (status != null && !status.matches("AGENDADO|CONCLUIDO|FOLGA|FALTA")) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }

    /**
     * Monta o DTO de resposta enriquecido com dados de funcionário e turno via JOIN em memória.
     * Os dados vêm dos repositórios JPA, não de colunas na tabela escalas.
     */
    private EscalaResponseDTO toDTO(Escala e) {
        String nomeFuncionario = null;
        String cargoFuncionario = null;
        String nomeTurno = null;
        String horaInicio = null;
        String horaFim = null;

        try {
            Funcionario f = funcionarioRepository.findById(e.getFuncionarioId()).orElse(null);
            if (f != null) {
                nomeFuncionario = f.getNome();
                cargoFuncionario = f.getCargo();
            }

            Turno t = turnoRepository.findById(e.getTurnoId()).orElse(null);
            if (t != null) {
                nomeTurno = t.getNome();
                horaInicio = t.getHoraInicio();
                horaFim = t.getHoraFim();
            }
        } catch (Exception ex) {
            // Caso algum vínculo não seja localizado, retorna os dados da escala sem quebrar a listagem
        }

        return new EscalaResponseDTO(
                e.getId(), e.getFuncionarioId(), e.getTurnoId(),
                e.getData(), e.getStatus(), e.getObservacao(),
                nomeFuncionario, cargoFuncionario, nomeTurno, horaInicio, horaFim
        );
    }
}