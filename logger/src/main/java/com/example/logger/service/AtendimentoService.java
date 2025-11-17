package com.example.logger.service;

import com.example.logger.domain.PacienteDTO;
import com.example.logger.model.entity.AtendimentoEntity;
import com.example.logger.model.entity.PacienteEntity;
import com.example.logger.model.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;

    private static final String STATUS_ALTA_PREFIX = "Alta - ";

    public AtendimentoService(AtendimentoRepository atendimentoRepository) {
        this.atendimentoRepository = atendimentoRepository;
    }

    @Transactional
    public void processarAtendimento(PacienteEntity paciente, PacienteDTO pacienteDTO) {

        // MUDANÇA: Chamando o novo método @Query 'findLatestOpenAtendimentoByPaciente'
        // MUDANÇA CRÍTICA: Devemos adicionar o wildcard '%' ao prefixo
        // para que a query 'NOT LIKE' funcione corretamente.
        String prefixoComWildcard = STATUS_ALTA_PREFIX + "%";

        Optional<AtendimentoEntity> atendimentoAberto =
                atendimentoRepository.findLatestOpenAtendimentoByPaciente(
                        paciente,
                        prefixoComWildcard
                );

        AtendimentoEntity atendimento;

        // O resto da sua lógica original está perfeita e permanece
        if (atendimentoAberto.isPresent()) {
            atendimento = atendimentoAberto.get();
            atendimento.setSintomas(pacienteDTO.getSintomas());

            if (pacienteDTO.getMedicoResponsavel() != null) {
                atendimento.setMedicoResponsavel(pacienteDTO.getMedicoResponsavel());
            }

        } else {
            atendimento = new AtendimentoEntity();
            atendimento.setPaciente(paciente);
            atendimento.setSintomas(pacienteDTO.getSintomas());
            atendimento.setDataAtendimento(LocalDate.now());

            String medico = (pacienteDTO.getMedicoResponsavel() != null)
                    ? pacienteDTO.getMedicoResponsavel()
                    : "Em triagem";
            atendimento.setMedicoResponsavel(medico);
        }

        atendimentoRepository.save(atendimento);
    }
}