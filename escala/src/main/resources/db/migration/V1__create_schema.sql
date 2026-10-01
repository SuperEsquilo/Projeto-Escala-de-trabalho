CREATE TABLE IF NOT EXISTS usuarios (
                                        id      SERIAL PRIMARY KEY,
                                        nome    VARCHAR(150) NOT NULL,
    email   VARCHAR(150) NOT NULL UNIQUE,
    senha   VARCHAR(255) NOT NULL,
    perfil  VARCHAR(20)  NOT NULL DEFAULT 'USUARIO'
    CHECK (perfil IN ('ADM_SUPREMO', 'ADMIN', 'USUARIO'))
    );

CREATE TABLE IF NOT EXISTS funcionarios (
                                            id                    SERIAL PRIMARY KEY,
                                            nome                  VARCHAR(150) NOT NULL,
    cargo                 VARCHAR(100) NOT NULL,
    cpf                   VARCHAR(14)  NOT NULL UNIQUE,
    telefone              VARCHAR(20),
    email                 VARCHAR(150),
    carga_horaria_semanal INTEGER DEFAULT 44,
    ativo                 BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS turnos (
                                      id             SERIAL PRIMARY KEY,
                                      nome           VARCHAR(100) NOT NULL,
    hora_inicio    VARCHAR(5)   NOT NULL,
    hora_fim       VARCHAR(5)   NOT NULL,
    duracao_horas  NUMERIC(4,1),
    descricao      VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS escalas (
                                       id              SERIAL PRIMARY KEY,
                                       funcionario_id  INTEGER NOT NULL REFERENCES funcionarios(id),
    turno_id        INTEGER NOT NULL REFERENCES turnos(id),
    data            VARCHAR(10)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'AGENDADO'
    CHECK (status IN ('AGENDADO', 'CONCLUIDO', 'FOLGA', 'FALTA')),
    observacao      VARCHAR(500),
    CONSTRAINT uk_escala_func_turno_data UNIQUE (funcionario_id, turno_id, data)
    );

CREATE INDEX IF NOT EXISTS idx_escalas_data           ON escalas(data);
CREATE INDEX IF NOT EXISTS idx_escalas_funcionario    ON escalas(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_funcionarios_ativo     ON funcionarios(ativo);
CREATE INDEX IF NOT EXISTS idx_usuarios_email         ON usuarios(email);