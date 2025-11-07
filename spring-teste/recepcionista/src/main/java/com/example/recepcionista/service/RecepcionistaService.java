package com.example.recepcionista.service;

import com.hospital.recepcionista.model.Paciente;
import com.hospital.recepcionista.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecepcionistaService {

    private static final Logger logger = LoggerFactory.getLogger(RecepcionistaService.class);
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String TRIAGEM_ROUTING_KEY = "paciente.triagem";

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public Paciente registrarPaciente(Paciente paciente) {
        paciente.setStatus("AGUARDANDO_TRIAGEM");
        paciente.setDataCadastro(LocalDateTime.now());

        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        
        logger.info("Paciente {} registrado no banco de dados com ID {}", 
                    pacienteSalvo.getNome(), pacienteSalvo.getId());

        try {
            rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                TRIAGEM_ROUTING_KEY,
                pacienteSalvo
            );
            logger.info("Paciente {} enviado para fila de triagem", pacienteSalvo.getNome());
        } catch (Exception e) {
            logger.error("Erro ao enviar paciente para RabbitMQ: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar paciente para triagem", e);
        }

        return pacienteSalvo;
    }

    public List<Paciente> listarTodosPacientes() {
        return pacienteRepository.findAll();
    }

    public Paciente buscarPacientePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com ID: " + id));
    }

    public List<Paciente> buscarPacientesPorStatus(String status) {
        return pacienteRepository.findByStatus(status);
    }
}
