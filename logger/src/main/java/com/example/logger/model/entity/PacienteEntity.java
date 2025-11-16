package com.example.logger.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
public class PacienteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(name = "sexo_biologico")
    private String sexoBiologico;

    @Column(name = "data_nascimento")
    private String dataNascimento;

    @Column(name = "tipo_sanguineo")
    private String tipoSanguineo;

    @Column(name = "estado_civil")
    private String estadoCivil;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AtendimentoEntity> atendimentos = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getSexoBiologico() { return sexoBiologico; }

    public void setSexoBiologico(String sexoBiologico) { this.sexoBiologico = sexoBiologico; }

    public String getDataNascimento() { return dataNascimento; }

    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTipoSanguineo() { return tipoSanguineo; }

    public void setTipoSanguineo(String tipoSanguineo) { this.tipoSanguineo = tipoSanguineo; }

    public String getEstadoCivil() { return estadoCivil; }

    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public List<AtendimentoEntity> getAtendimentos() { return atendimentos; }

    public void setAtendimentos(List<AtendimentoEntity> atendimentos) { this.atendimentos = atendimentos; }
}