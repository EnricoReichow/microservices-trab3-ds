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
    private static final String STATUS_ALTA = "Nenhum (teve alta)";

    public AtendimentoService(AtendimentoRepository atendimentoRepository) {
        this.atendimentoRepository = atendimentoRepository;
    }

    /**
     * Processa o atendimento do paciente vindo da fila.
     * - Se o paciente não tiver um atendimento "aberto", cria um novo.
     * - Se o paciente tiver um atendimento "aberto", atualiza-o.
     *
     * @param paciente O PacienteEntity (já processado pelo PacienteService)
     * @param pacienteDTO O DTO original da fila (para pegar sintomas e médico)
     */
    @Transactional
    public void processarAtendimento(PacienteEntity paciente, PacienteDTO pacienteDTO) {

        // 1. Busca o último atendimento ABERTO (que NÃO SEJA "alta")
        Optional<AtendimentoEntity> atendimentoAberto =
                atendimentoRepository.findFirstByPacienteAndMedicoResponsavelNotOrderByIdDesc(paciente, STATUS_ALTA);

        AtendimentoEntity atendimento;

        if (atendimentoAberto.isPresent()) {
            // 2. ATENDIMENTO ABERTO EXISTE: Atualiza
            atendimento = atendimentoAberto.get();
            atendimento.setSintomas(pacienteDTO.getSintomas());

            // Atualiza o médico se vier no DTO (ex: "Em triagem", ou "Dr. House")
            if (pacienteDTO.getMedicoResponsavel() != null) {
                atendimento.setMedicoResponsavel(pacienteDTO.getMedicoResponsavel());
            }
            // A data do atendimento não muda, pois estamos atualizando o mesmo atendimento

        } else {
            // 3. NÃO HÁ ATENDIMENTO ABERTO: Cria um novo
            atendimento = new AtendimentoEntity();
            atendimento.setPaciente(paciente);
            atendimento.setSintomas(pacienteDTO.getSintomas());
            atendimento.setDataAtendimento(LocalDate.now()); // Data de hoje

            // Define o médico. Se vier nulo da fila, define "Em triagem".
            String medico = (pacienteDTO.getMedicoResponsavel() != null)
                    ? pacienteDTO.getMedicoResponsavel()
                    : "Em triagem";
            atendimento.setMedicoResponsavel(medico);
        }

        atendimentoRepository.save(atendimento);
    }
}