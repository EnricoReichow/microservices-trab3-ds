package com.example.logger.consumer;

import com.example.logger.domain.PacienteDTO;
import com.example.logger.model.entity.PacienteEntity;
import com.example.logger.service.AtendimentoService;
import com.example.logger.service.PacienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PacienteConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacienteConsumer.class);

    private final PacienteService pacienteService;
    private final AtendimentoService atendimentoService;

    public PacienteConsumer(PacienteService pacienteService, AtendimentoService atendimentoService) {
        this.pacienteService = pacienteService;
        this.atendimentoService = atendimentoService;
    }

    /**
     * Escuta a fila 'loggerQueue' e processa as mensagens.
     */
    @RabbitListener(queues = {"${app.rabbitmq.queue}"})
    public void receiveMessage(@Payload PacienteDTO pacienteDTO) {
        LOGGER.info("Mensagem recebida da fila: {}", pacienteDTO.toString());

        try {
            // 1. Processa (cria ou atualiza) o cadastro do paciente
            PacienteEntity paciente = pacienteService.processarPaciente(pacienteDTO);

            // 2. Processa (cria ou atualiza) o atendimento
            atendimentoService.processarAtendimento(paciente, pacienteDTO);

            LOGGER.info("Paciente {} processado com sucesso (ID: {})", paciente.getNome(), paciente.getId());

        } catch (Exception e) {
            // Se falhar, a mensagem deve ir para uma Dead Letter Queue (DLQ)
            // (Configuração de DLQ não inclusa neste escopo, mas é recomendada)
            LOGGER.error("Erro ao processar paciente da fila: {}", e.getMessage(), e);
            // Re-lança a exceção para o Spring AMQP tratar (ex: retry, DLQ)
            throw new RuntimeException("Falha no processamento da mensagem do paciente.", e);
        }
    }
}