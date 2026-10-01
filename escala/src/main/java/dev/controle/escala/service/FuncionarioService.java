package dev.controle.escala.service;

import dev.controle.escala.model.Funcionario;
import dev.controle.escala.repository.EscalaRepository;
import dev.controle.escala.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuncionarioService {
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private EscalaRepository escalaRepository;

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    public List<Funcionario> listarAtivos() {
        return funcionarioRepository.findByAtivo(true);
    }

    public Funcionario buscarPorId(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com id: " + id));
    }

    @Transactional
    public Funcionario salvar(Funcionario f) {
        if (f.getNome() == null || f.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (f.getCargo() == null || f.getCargo().isBlank()) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }
        if (f.getCpf() == null || f.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (funcionarioRepository.existsByCpf(f.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (f.getCargaHorariaSemanal() == null) {
            f.setCargaHorariaSemanal(44);
        }
        if (f.getAtivo() == null) {
            f.setAtivo(true);
        }
        return funcionarioRepository.save(f);
    }

    @Transactional
    public Funcionario atualizar(Integer id, Funcionario f) {
        Funcionario existente = buscarPorId(id);
        if (f.getCpf() != null && !f.getCpf().equals(existente.getCpf())) {
            if (funcionarioRepository.existsByCpfAndIdNot(f.getCpf(), id)) {
                throw new IllegalArgumentException("CPF já cadastrado para outro funcionário");
            }
        }
        existente.setNome(f.getNome() != null ? f.getNome() : existente.getNome());
        existente.setCargo(f.getCargo() != null ? f.getCargo() : existente.getCargo());
        existente.setCpf(f.getCpf() != null ? f.getCpf() : existente.getCpf());
        existente.setTelefone(f.getTelefone());
        existente.setEmail(f.getEmail());
        existente.setCargaHorariaSemanal(f.getCargaHorariaSemanal() != null ? f.getCargaHorariaSemanal() : existente.getCargaHorariaSemanal());
        existente.setAtivo(f.getAtivo() != null ? f.getAtivo() : existente.getAtivo());
        return funcionarioRepository.save(existente);
    }

    /**
     * Soft delete: marca como inativo em vez de apagar do banco.
     * Preserva histórico de escalas associadas.
     */
    @Transactional
    public void desativar(Integer id) {
        Funcionario funcionario = buscarPorId(id);
        funcionario.setAtivo(false);
        funcionarioRepository.save(funcionario);
    }

    /**
     * Hard delete: só permitido se não houver escalas vinculadas.
     */
    @Transactional
    public void deletar(Integer id) {
        buscarPorId(id); // garante que existe
        funcionarioRepository.deleteById(id);
    }
}