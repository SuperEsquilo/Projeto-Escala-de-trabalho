package dev.controle.escala.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Nome do turno é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Hora de início é obrigatória")
    @Column(name = "hora_inicio", nullable = false, length = 5)
    private String horaInicio; // HH:mm

    @NotBlank(message = "Hora de fim é obrigatória")
    @Column(name = "hora_fim", nullable = false, length = 5)
    private String horaFim;   // HH:mm

    // Calculado no servidor a partir de horaInicio e horaFim — não enviado pelo cliente
    @Column(name = "duracao_horas")
    private Double duracaoHoras;

    @Column(length = 255)
    private String descricao;

    public Turno() {
    }

    public Turno(String nome, String horaInicio, String horaFim, Double duracaoHoras, String descricao) {
        this.nome = nome;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.duracaoHoras = duracaoHoras;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }

    public Double getDuracaoHoras() {
        return duracaoHoras;
    }

    public void setDuracaoHoras(Double duracaoHoras) {
        this.duracaoHoras = duracaoHoras;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}