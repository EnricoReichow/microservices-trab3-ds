package com.example.triagem_sender.listener;

import com.hospital.triagem.model.Paciente;
import com.hospital.triagem.service.TriagemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriagemListener {

    private static final Logger logger = LoggerFactory.getLogger(TriagemListener.class);

    @Autowired
    private TriagemService triagemService;

    /**
     * Escuta a fila de triagem e processa pacientes recebidos
     */
    @RabbitListener(queues = "triagem")
    public void receberPaciente(Paciente paciente) {
        logger.info("Paciente recebido na triagem: {} com sintoma: {}", 
                   paciente.getNome(), paciente.getSintomas());
        
        try {
            // Realiza a triagem e encaminha para o especialista apropriado
            triagemService.realizarTriagem(paciente);
            
        } catch (Exception e) {
            logger.error("Erro ao processar paciente na triagem: {}", e.getMessage(), e);
            throw e;
        }
    }
}
