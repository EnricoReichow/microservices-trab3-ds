package com.example.logger.model.repository;

import com.example.logger.model.entity.AtendimentoEntity;
import com.example.logger.model.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AtendimentoRepository extends JpaRepository<AtendimentoEntity, Long> {

    /**
     * Busca o último atendimento de um paciente (ordenado por ID decrescente)
     * que NÃO tenha o médico "Nenhum (teve alta)".
     * Isso nos dá o último atendimento "aberto".
     */
    Optional<AtendimentoEntity> findFirstByPacienteAndMedicoResponsavelNotOrderByIdDesc(
            PacienteEntity paciente,
            String medicoStatusAlta
    );
}