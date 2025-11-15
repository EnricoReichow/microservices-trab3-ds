package com.example.recepcionista_backend.model;

public class Paciente {

    private String nome;
    private String sexoBiologico;
    private String dataNascimento;
    private String tipoSanguineo;
    private String estadoCivil;
    private String sintomas;
    private String medicoResponsavel;

    // Construtor vazio (boa prática)
    public Paciente() {
    }

    public Paciente(String nome, String sexoBiologico, String dataNascimento, String tipoSanguineo, String estadoCivil, String sintomas, String medicoResponsavel) {
        this.nome = nome;
        this.sexoBiologico = sexoBiologico;
        this.dataNascimento = dataNascimento;
        this.tipoSanguineo = tipoSanguineo;
        this.estadoCivil = estadoCivil;
        this.sintomas = sintomas;
        this.medicoResponsavel = medicoResponsavel;
    }

    public Paciente(String nome, String sexoBiologico, String dataNascimento, String tipoSanguineo, String estadoCivil, String sintomas) {
        this.nome = nome;
        this.sexoBiologico = sexoBiologico;
        this.dataNascimento = dataNascimento;
        this.tipoSanguineo = tipoSanguineo;
        this.estadoCivil = estadoCivil;
        this.sintomas = sintomas;
    }

    // Getters e Setters (importantes para o Spring converter para JSON)

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

    @Override
    public String toString() {
        return "Paciente{" +
                "nome='" + nome + '\'' +
                ", sexoBiologico='" + sexoBiologico + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", tipoSanguineo='" + tipoSanguineo + '\'' +
                ", estadoCivil='" + estadoCivil + '\'' +
                ", sintomas='" + sintomas + '\'' +
                '}';
    }
}