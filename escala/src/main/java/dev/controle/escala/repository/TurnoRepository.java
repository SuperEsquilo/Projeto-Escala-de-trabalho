package dev.controle.escala.repository;

import dev.controle.escala.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Integer> {
    boolean existsByNome(String nome);
}