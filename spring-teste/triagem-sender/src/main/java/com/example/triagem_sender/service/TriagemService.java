package com.example.triagem_sender.service;

import com.hospital.triagem.model.Paciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriagemService {

    private static final Logger logger = LoggerFactory.getLogger(TriagemService.class);
    
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String CARDIO_ROUTING_KEY = "paciente.cardiologia";
    private static final String NEURO_ROUTING_KEY = "paciente.neurologia";
    private static final String GERAL_ROUTING_KEY = "paciente.geral";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Realiza triagem do paciente baseado nos sintomas e encaminha para especialista apropriado
     */
    public void realizarTriagem(Paciente paciente) {
        String sintoma = paciente.getSintomas();
        String destinoKey;

        // Lógica de triagem baseada nos sintomas
        if ("Infarto".equalsIgnoreCase(sintoma) || "Pressão alta".equalsIgnoreCase(sintoma)) {
            // Casos cardiológicos
            paciente.setMedicoResponsavel("Cardiologista");
            destinoKey = CARDIO_ROUTING_KEY;
            logger.info("Paciente {} com sintoma '{}' encaminhado para Cardiologista", 
                       paciente.getNome(), sintoma);
            
        } else if ("Dor de cabeça".equalsIgnoreCase(sintoma) || 
                   "Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
            // Casos neurológicos
            paciente.setMedicoResponsavel("Neurologista");
            destinoKey = NEURO_ROUTING_KEY;
            logger.info("Paciente {} com sintoma '{}' encaminhado para Neurologista", 
                       paciente.getNome(), sintoma);
            
        } else {
            // Demais casos vão para clínica geral
            paciente.setMedicoResponsavel("Clínico Geral");
            destinoKey = GERAL_ROUTING_KEY;
            logger.info("Paciente {} com sintoma '{}' encaminhado para Clínico Geral", 
                       paciente.getNome(), sintoma);
        }

        // Envia para a fila do especialista apropriado
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, destinoKey, paciente);
        logger.debug("Mensagem enviada para exchange '{}' com routing key '{}'", EXCHANGE_NAME, destinoKey);
    }
}
