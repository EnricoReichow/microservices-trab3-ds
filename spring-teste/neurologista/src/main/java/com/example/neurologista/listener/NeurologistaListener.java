package com.example.neurologista.listener;

import com.hospital.common.config.RabbitMQConfig;
import com.hospital.common.model.Paciente;
import com.hospital.neurologista.service.NeurologistaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NeurologistaListener {

    private static final Logger logger = LoggerFactory.getLogger(NeurologistaListener.class);

    @Autowired
    private NeurologistaService neurologistaService;

    @RabbitListener(queues = RabbitMQConfig.NEURO_QUEUE)
    public void receberPaciente(Paciente paciente) {
        logger.info("Paciente recebido na neurologia: {} com sintoma: {}", 
                    paciente.getNome(), paciente.getSintomas());
        
        try {
            neurologistaService.atenderPaciente(paciente);
        } catch (Exception e) {
            logger.error("Erro ao atender paciente {}: {}", 
                        paciente.getNome(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FALLBACK_NEURO_QUEUE)
    public void receberPacienteFallback(Paciente paciente) {
        logger.info("Paciente recebido na fila de fallback neurologia: {} com sintoma: {}", 
                    paciente.getNome(), paciente.getSintomas());
        
        try {
            neurologistaService.atenderPaciente(paciente);
        } catch (Exception e) {
            logger.error("Erro ao atender paciente do fallback {}: {}", 
                        paciente.getNome(), e.getMessage(), e);
        }
    }
}
