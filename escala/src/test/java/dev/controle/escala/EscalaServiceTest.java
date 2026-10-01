package dev.controle.escala;

import dev.controle.escala.dto.EscalaResponseDTO;
import dev.controle.escala.model.Escala;
import dev.controle.escala.model.Funcionario;
import dev.controle.escala.model.Turno;
import dev.controle.escala.repository.EscalaRepository;
import dev.controle.escala.repository.FuncionarioRepository;
import dev.controle.escala.repository.TurnoRepository;
import dev.controle.escala.service.EscalaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscalaServiceTest {

    @Mock
    private EscalaRepository escalaRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private EscalaService escalaService;

    @Test
    @DisplayName("Deve impedir agendamento de funcionário inativo")
    void deveImpedirEscalaFuncionarioInativo() {
        Funcionario inativo = new Funcionario();
        inativo.setId(1);
        inativo.setNome("José");
        inativo.setAtivo(false); // Inativo

        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(inativo));

        Escala escala = new Escala();
        escala.setFuncionarioId(1);
        escala.setTurnoId(2);
        escala.setData("2026-09-30");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> escalaService.salvar(escala));

        assertTrue(ex.getMessage().contains("inativo"));
        verify(escalaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve impedir agendamento em turnos com sobreposição de horário no mesmo dia")
    void deveImpedirSobreposicaoDeHorario() {
        Funcionario ativo = new Funcionario();
        ativo.setId(1);
        ativo.setNome("Maria");
        ativo.setAtivo(true);

        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(ativo));
        when(turnoRepository.existsById(2)).thenReturn(true);
        when(escalaRepository.existeSobreposicaoHorario(eq(1), eq(2), eq("2026-09-30"), eq(-1)))
                .thenReturn(true); // Houve sobreposição

        Escala escala = new Escala();
        escala.setFuncionarioId(1);
        escala.setTurnoId(2);
        escala.setData("2026-09-30");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> escalaService.salvar(escala));

        assertTrue(ex.getMessage().contains("sobreposto"));
        verify(escalaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar escala e retornar DTO com dados enriquecidos via JOIN")
    void deveSalvarERetornarDTOEnriquecido() {
        Funcionario func = new Funcionario();
        func.setId(1);
        func.setNome("Carlos Silva");
        func.setCargo("Operador");
        func.setAtivo(true);

        Turno turno = new Turno();
        turno.setId(2);
        turno.setNome("Tarde");
        turno.setHoraInicio("13:00");
        turno.setHoraFim("22:00");

        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(func));
        when(turnoRepository.existsById(2)).thenReturn(true);
        when(turnoRepository.findById(2)).thenReturn(Optional.of(turno));
        when(escalaRepository.existeSobreposicaoHorario(1, 2, "2026-09-30", -1)).thenReturn(false);

        when(escalaRepository.save(any(Escala.class))).thenAnswer(inv -> {
            Escala e = inv.getArgument(0);
            e.setId(99);
            return e;
        });

        Escala nova = new Escala();
        nova.setFuncionarioId(1);
        nova.setTurnoId(2);
        nova.setData("2026-09-30");

        EscalaResponseDTO dto = escalaService.salvar(nova);

        assertNotNull(dto);
        assertEquals(99, dto.getId());
        assertEquals("Carlos Silva", dto.getNomeFuncionario());
        assertEquals("Operador", dto.getCargoFuncionario());
        assertEquals("Tarde", dto.getNomeTurno());
        assertEquals("13:00", dto.getHoraInicioTurno());
        assertEquals("22:00", dto.getHoraFimTurno());
    }
}
