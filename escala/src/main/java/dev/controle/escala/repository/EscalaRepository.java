package dev.controle.escala.repository;

import dev.controle.escala.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Integer> {
    List<Escala> findByData(String data);

    boolean existsByFuncionarioIdAndTurnoIdAndData(Integer funcionarioId, Integer turnoId, String data);

    boolean existsByFuncionarioIdAndTurnoIdAndDataAndIdNot(Integer funcionarioId, Integer turnoId, String data, Integer id);

    /**
     * Verifica sobreposição de horário: se o funcionário já tem uma escala
     * no mesmo dia com turno cujo horário se sobrepõe ao turno candidato.
     */
    @Query("""
                SELECT COUNT(e) > 0 FROM Escala e
                JOIN Turno t ON t.id = e.turnoId
                JOIN Turno novo ON novo.id = :turnoId
                WHERE e.funcionarioId = :funcionarioId
                  AND e.data = :data
                  AND e.id <> :excludeId
                  AND t.horaInicio < novo.horaFim
                  AND t.horaFim > novo.horaInicio
            """)
    boolean existeSobreposicaoHorario(
            @Param("funcionarioId") Integer funcionarioId,
            @Param("turnoId") Integer turnoId,
            @Param("data") String data,
            @Param("excludeId") Integer excludeId
    );
}