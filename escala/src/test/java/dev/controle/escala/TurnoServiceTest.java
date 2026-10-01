package dev.controle.escala;

import dev.controle.escala.model.Turno;
import dev.controle.escala.repository.TurnoRepository;
import dev.controle.escala.service.TurnoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private TurnoService turnoService;

    @Test
    @DisplayName("Deve calcular duração automaticamente e salvar turno com id único Integer")
    void deveSalvarTurnoComDuracaoCalculada() {
        Turno turno = new Turno();
        turno.setNome("Manhã");
        turno.setHoraInicio("08:00");
        turno.setHoraFim("17:00");

        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocation -> {
            Turno t = invocation.getArgument(0);
            t.setId(1); // ID inteiro único gerado
            return t;
        });

        Turno salvo = turnoService.salvar(turno);

        assertNotNull(salvo.getId());
        assertEquals(1, salvo.getId());
        assertEquals(9.0, salvo.getDuracaoHoras(), 0.01);
        verify(turnoRepository).save(turno);
    }

    @Test
    @DisplayName("Deve lançar erro quando hora fim for anterior ou igual à hora de início")
    void deveLancarErroQuandoHorarioInvalido() {
        Turno turno = new Turno();
        turno.setNome("Invalido");
        turno.setHoraInicio("18:00");
        turno.setHoraFim("08:00");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> turnoService.salvar(turno));
        assertTrue(ex.getMessage().contains("posterior"));
        verify(turnoRepository, never()).save(any());
    }
}
