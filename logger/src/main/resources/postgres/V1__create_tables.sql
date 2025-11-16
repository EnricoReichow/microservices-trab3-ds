-- Migration V1: Criação das tabelas principais

-- Tabela de Pacientes (Cadastro Único)
CREATE TABLE pacientes (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sexo_biologico VARCHAR(50),
    data_nascimento VARCHAR(20), -- Mantido como VARCHAR conforme DTO
    tipo_sanguineo VARCHAR(10),
    estado_civil VARCHAR(50),

    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_modificacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Garante que cada paciente (baseado no nome) seja único
    CONSTRAINT uk_paciente_nome UNIQUE(nome)
);

-- Tabela de Atendimentos (Múltiplos registros por paciente)
CREATE TABLE atendimentos (
    id SERIAL PRIMARY KEY,
    paciente_id INTEGER NOT NULL,
    sintomas TEXT,
    medico_responsavel VARCHAR(100),

    -- Usamos DATE (dia) conforme solicitado
    data_atendimento DATE NOT NULL DEFAULT CURRENT_DATE,

    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_modificacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_atendimento_paciente
        FOREIGN KEY(paciente_id)
        REFERENCES pacientes(id)
        ON DELETE CASCADE -- Se o paciente for deletado, seus atendimentos tbm são
);

-- Índices para otimizar buscas
CREATE INDEX idx_atendimento_paciente_id ON atendimentos(paciente_id);
CREATE INDEX idx_atendimento_data ON atendimentos(data_atendimento);