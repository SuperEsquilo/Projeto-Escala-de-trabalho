package dev.controle.escala.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "escalas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_escala_func_turno_data",
                columnNames = {"funcionario_id", "turno_id", "data"}
        )
)
public class Escala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "Funcionário é obrigatório")
    @Column(name = "funcionario_id", nullable = false)
    private Integer funcionarioId;
    @NotNull(message = "Turno é obrigatório")
    @Column(name = "turno_id", nullable = false)
    private Integer turnoId;
    @NotBlank(message = "Data é obrigatória")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Data deve estar no formato YYYY-MM-DD")
    @Column(name = "data", nullable = false, length = 10)
    private String data;
    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "AGENDADO|CONCLUIDO|FOLGA|FALTA", message = "Status inválido")
    @Column(nullable = false, length = 20)
    private String status;
    @Column(length = 500)
    private String observacao;

    public Escala() {
    }

    public Escala(Integer funcionarioId, Integer turnoId, String data, String status, String observacao) {
        this.funcionarioId = funcionarioId;
        this.turnoId = turnoId;
        this.data = data;
        this.status = status;
        this.observacao = observacao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Integer funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public Integer getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(Integer turnoId) {
        this.turnoId = turnoId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}