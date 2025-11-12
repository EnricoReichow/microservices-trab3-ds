package com.example.triagem_sender.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "sexo_biologico")
    private String sexoBiologico;

    @Column(name = "data_nascimento")
    private String dataNascimento;

    @Column(name = "tipo_sanguineo")
    private String tipoSanguineo;

    @Column(name = "estado_civil")
    private String estadoCivil;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(name = "medico_responsavel")
    private String medicoResponsavel;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_consulta")
    private LocalDateTime dataConsulta;

    @Column(name = "status")
    private String status;

    public Paciente(String nome, String sexoBiologico, String dataNascimento, 
                    String tipoSanguineo, String estadoCivil, String sintomas) {
        this.nome = nome;
        this.sexoBiologico = sexoBiologico;
        this.dataNascimento = dataNascimento;
        this.tipoSanguineo = tipoSanguineo;
        this.estadoCivil = estadoCivil;
        this.sintomas = sintomas;
        this.dataCadastro = LocalDateTime.now();
        this.status = "AGUARDANDO_TRIAGEM";
    }
}
