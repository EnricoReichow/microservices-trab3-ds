package com.example.logger.model.repository;

import com.example.logger.model.entity.AtendimentoEntity;
import com.example.logger.model.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AtendimentoRepository extends JpaRepository<AtendimentoEntity, Long> {


    @Query("SELECT a FROM AtendimentoEntity a " +
            "WHERE a.paciente = :paciente " +
            "AND a.medicoResponsavel NOT LIKE :prefixoAlta " +
            "ORDER BY a.id DESC")
    Optional<AtendimentoEntity> findLatestOpenAtendimentoByPaciente(
            @Param("paciente") PacienteEntity paciente,
            @Param("prefixoAlta") String prefixoAlta
    );
}