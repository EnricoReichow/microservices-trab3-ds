// src/main/java/com/example/logger/service/AtendimentoService.java
package com.example.logger.service;

import com.example.logger.domain.PacienteDTO;
import com.example.logger.model.entity.AtendimentoEntity;
import com.example.logger.model.entity.PacienteEntity;
import com.example.logger.model.repository.AtendimentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AtendimentoService {

    private static final Logger logger = LoggerFactory.getLogger(AtendimentoService.class);

    private final AtendimentoRepository atendimentoRepository;
    private final TelegramService telegramService;

    @Value("${telegram.token}")
    private String telegramToken;

    @Value("${telegram.defaultChatId}")
    private String defaultChatId;

    @Value("${telegram.chat.cardiologista:${telegram.defaultChatId}}")
    private String cardiologistaChatId;

    @Value("${telegram.chat.neurologista:${telegram.defaultChatId}}")
    private String neurologistaChatId;

    @Value("${telegram.chat.clinicoGeral:${telegram.defaultChatId}}")
    private String clinicoGeralChatId;

    private static final String ATENDIDO_PELO_PREFIX = "Atendido pelo ";

    public AtendimentoService(AtendimentoRepository atendimentoRepository, TelegramService telegramService) {
        this.atendimentoRepository = atendimentoRepository;
        this.telegramService = telegramService;
    }

    @Transactional
    public void processarAtendimento(PacienteEntity paciente, PacienteDTO pacienteDTO) {

        String prefixoComWildcard = "Alta - %";
        Optional<AtendimentoEntity> atendimentoAberto = atendimentoRepository.findLatestOpenAtendimentoByPaciente(paciente, prefixoComWildcard);
        AtendimentoEntity atendimento;

        String sintomas = pacienteDTO.getSintomas();
        String medicoResponsavel;

        if (atendimentoAberto.isPresent()) {
            atendimento = atendimentoAberto.get();
            atendimento.setSintomas(sintomas);

            if (pacienteDTO.getMedicoResponsavel() != null) {
                atendimento.setMedicoResponsavel(pacienteDTO.getMedicoResponsavel());
                medicoResponsavel = pacienteDTO.getMedicoResponsavel();
            } else {
                medicoResponsavel = atendimento.getMedicoResponsavel();
            }

        } else {
            atendimento = new AtendimentoEntity();
            atendimento.setPaciente(paciente);
            atendimento.setSintomas(sintomas);
            atendimento.setDataAtendimento(LocalDate.now());

            medicoResponsavel = (pacienteDTO.getMedicoResponsavel() != null)
                    ? pacienteDTO.getMedicoResponsavel()
                    : "Em triagem";
            atendimento.setMedicoResponsavel(medicoResponsavel);
        }

        atendimentoRepository.save(atendimento);

        // Only send Telegram when medicoResponsavel indicates discharge by a specialist:
        // e.g. "Atendido pelo Cardiologista", "Atendido pelo Neurologista", "Atendido pelo Clínico Geral"
        if (medicoResponsavel != null && medicoResponsavel.startsWith(ATENDIDO_PELO_PREFIX)) {
            String especialista = medicoResponsavel.substring(ATENDIDO_PELO_PREFIX.length()).trim();
            String chatId = determineChatIdBySpecialist(especialista);
            String message = String.format("Novo atendimento - Alta\nPaciente: %s\nData: %s\nEspecialista: %s\nSintomas: %s", paciente.getNome(), LocalDate.now(), especialista, sintomas);

            try {
                telegramService.sendMessage(telegramToken, chatId, message);
            } catch (Exception ex) {
                logger.warn("Falha ao enviar mensagem Telegram: {}", ex.getMessage());
            }
        }
    }

    // Novo método adicionado para listar todos os atendimentos
    public List<AtendimentoEntity> listarTodosAtendimentos() {
        return atendimentoRepository.findAll();
    }

    private String determineChatIdBySpecialist(String especialista) {
        String normalized = normalize(especialista).toLowerCase();

        if (normalized.contains("cardiolog")) {
            return cardiologistaChatId;
        } else if (normalized.contains("neurolog")) {
            return neurologistaChatId;
        } else if (normalized.contains("clin") || normalized.contains("clinico") || normalized.contains("clinico geral")) {
            return clinicoGeralChatId;
        } else {
            return defaultChatId;
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents;
    }
}
