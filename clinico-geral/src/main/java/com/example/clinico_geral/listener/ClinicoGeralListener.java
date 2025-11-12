package com.example.clinico_geral.listener;

import com.example.clinico_geral.config.ClinicoGeralRabbitMQConfig;
import com.example.clinico_geral.model.Paciente;
import com.example.clinico_geral.service.ClinicoGeralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinicoGeralListener {

    private static final Logger logger = LoggerFactory.getLogger(ClinicoGeralListener.class);

    @Autowired
    private ClinicoGeralService clinicoGeralService;

    @RabbitListener(queues = ClinicoGeralRabbitMQConfig.GERAL_QUEUE)
    public void receberPaciente(Paciente paciente) {
        logger.info("Paciente recebido no clínico geral: {} com sintoma: {}", 
                    paciente.getNome(), paciente.getSintomas());
        
        try {
            clinicoGeralService.atenderPaciente(paciente);
        } catch (Exception e) {
            logger.error("Erro ao atender paciente {}: {}", 
                        paciente.getNome(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = ClinicoGeralRabbitMQConfig.FALLBACK_GERAL_QUEUE)
    public void receberPacienteFallback(Paciente paciente) {
        logger.info("Paciente recebido na fila de fallback clínico geral: {} com sintoma: {}", 
                    paciente.getNome(), paciente.getSintomas());
        
        try {
            clinicoGeralService.atenderPaciente(paciente);
        } catch (Exception e) {
            logger.error("Erro ao atender paciente do fallback {}: {}", 
                        paciente.getNome(), e.getMessage(), e);
        }
    }
}
