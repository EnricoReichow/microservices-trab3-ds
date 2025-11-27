package com.example.logger.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "atendimentos")
public class AtendimentoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    @JsonIgnore
    private PacienteEntity paciente;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(name = "medico_responsavel")
    private String medicoResponsavel;

    @Column(name = "data_atendimento", nullable = false)
    private LocalDate dataAtendimento;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public PacienteEntity getPaciente() { return paciente; }

    public void setPaciente(PacienteEntity paciente) { this.paciente = paciente; }

    public String getSintomas() { return sintomas; }

    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public String getMedicoResponsavel() { return medicoResponsavel; }

    public void setMedicoResponsavel(String medicoResponsavel) { this.medicoResponsavel = medicoResponsavel; }

    public LocalDate getDataAtendimento() { return dataAtendimento; }

    public void setDataAtendimento(LocalDate dataAtendimento) { this.dataAtendimento = dataAtendimento; }
}