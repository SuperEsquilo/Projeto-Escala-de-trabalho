package dev.controle.escala.dto;

public class EscalaResponseDTO {
    private Integer id;
    private Integer funcionarioId;
    private Integer turnoId;
    private String data;
    private String status;
    private String observacao;
    // Campos de exibição — vêm do JOIN, não do banco de escalas
    private String nomeFuncionario;
    private String cargoFuncionario;
    private String nomeTurno;
    private String horaInicioTurno;
    private String horaFimTurno;

    public EscalaResponseDTO() {
    }

    public EscalaResponseDTO(Integer id, Integer funcionarioId, Integer turnoId,
                             String data, String status, String observacao,
                             String nomeFuncionario, String cargoFuncionario,
                             String nomeTurno, String horaInicioTurno, String horaFimTurno) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.turnoId = turnoId;
        this.data = data;
        this.status = status;
        this.observacao = observacao;
        this.nomeFuncionario = nomeFuncionario;
        this.cargoFuncionario = cargoFuncionario;
        this.nomeTurno = nomeTurno;
        this.horaInicioTurno = horaInicioTurno;
        this.horaFimTurno = horaFimTurno;
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

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public void setNomeFuncionario(String nomeFuncionario) {
        this.nomeFuncionario = nomeFuncionario;
    }

    public String getCargoFuncionario() {
        return cargoFuncionario;
    }

    public void setCargoFuncionario(String cargoFuncionario) {
        this.cargoFuncionario = cargoFuncionario;
    }

    public String getNomeTurno() {
        return nomeTurno;
    }

    public void setNomeTurno(String nomeTurno) {
        this.nomeTurno = nomeTurno;
    }

    public String getHoraInicioTurno() {
        return horaInicioTurno;
    }

    public void setHoraInicioTurno(String horaInicioTurno) {
        this.horaInicioTurno = horaInicioTurno;
    }

    public String getHoraFimTurno() {
        return horaFimTurno;
    }

    public void setHoraFimTurno(String horaFimTurno) {
        this.horaFimTurno = horaFimTurno;
    }
}