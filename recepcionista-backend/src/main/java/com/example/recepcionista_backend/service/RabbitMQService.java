package com.example.recepcionista_backend.service;

import com.example.recepcionista_backend.model.Paciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQService.class);

    // Injeta o template configurado no RabbitMQConfig
    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKeyTriagem; // Chave da triagem
    private final String routingKeyLog;     // Nova chave do logger

    public RabbitMQService(RabbitTemplate rabbitTemplate,
                           @Value("${app.rabbitmq.exchange}") String exchangeName,
                           @Value("${app.rabbitmq.routingkey}") String routingKeyTriagem,
                           @Value("${app.rabbitmq.routingkey-log}") String routingKeyLog) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKeyTriagem = routingKeyTriagem;
        this.routingKeyLog = routingKeyLog;
    }

    /**
     * Envia um objeto Paciente para o RabbitMQ.
     * O RabbitTemplate cuidará da serialização para JSON.
     *
     * @param paciente O objeto Paciente a ser enviado.
     */
    public void sendPaciente(Paciente paciente) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKeyTriagem, paciente);
            LOGGER.info("Paciente enviado para TRIAGEM [RK={}]: {}", routingKeyTriagem, paciente.getNome());
        } catch (Exception e) {
            LOGGER.error("Erro ao enviar paciente para o RabbitMQ: {}", e.getMessage(), e);
        }

        // 2. Envia para a fila de Logger
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKeyLog, paciente);
            LOGGER.info("Paciente enviado para LOGGER [RK={}]: {}", routingKeyLog, paciente.getNome());
        } catch (Exception e) {
            LOGGER.error("Erro ao enviar paciente para LOGGER [RK={}]: {}", routingKeyLog, e.getMessage(), e);
        }
    }
}