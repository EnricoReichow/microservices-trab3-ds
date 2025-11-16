package com.example.logger.model.repository;

import com.example.logger.model.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    /**
     * Busca um paciente pelo nome (que é único).
     */
    Optional<PacienteEntity> findByNome(String nome);
}