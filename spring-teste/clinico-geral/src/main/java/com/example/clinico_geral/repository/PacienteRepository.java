package com.example.clinico_geral.repository;

import com.example.clinico_geral.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByStatus(String status);
    List<Paciente> findByMedicoResponsavel(String medicoResponsavel);
}
