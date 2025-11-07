package com.example.recepcionista.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequest {
    private String nome;
    private String sexoBiologico;
    private String dataNascimento;
    private String tipoSanguineo;
    private String estadoCivil;
    private String sintomas;
}
