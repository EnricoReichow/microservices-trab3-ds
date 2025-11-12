package com.example.cardiologista.model;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSexoBiologico() {
        return sexoBiologico;
    }

    public void setSexoBiologico(String sexoBiologico) {
        this.sexoBiologico = sexoBiologico;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(String tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public String getMedicoResponsavel() {
        return medicoResponsavel;
    }

    public void setMedicoResponsavel(String medicoResponsavel) {
        this.medicoResponsavel = medicoResponsavel;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
