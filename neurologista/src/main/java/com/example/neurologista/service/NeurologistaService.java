package com.example.neurologista.service;

import com.hospital.common.model.Paciente;
import com.hospital.neurologista.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NeurologistaService {

    private static final Logger logger = LoggerFactory.getLogger(NeurologistaService.class);

    @Autowired
    private PacienteRepository pacienteRepository;

    @Transactional
    public void atenderPaciente(Paciente paciente) {
        logger.info("Neurologista atendendo paciente: {} com sintoma: {}", 
                    paciente.getNome(), paciente.getSintomas());

        // Simula tempo de consulta (10 segundos)
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Consulta interrompida", e);
        }

        // Atualiza informações do paciente
        paciente.setStatus("FINALIZADO");
        paciente.setDataConsulta(LocalDateTime.now());

        // Salva no banco de dados
        Paciente pacienteSalvo = pacienteRepository.save(paciente);
        
        logger.info("Paciente {} consultado pelo Neurologista e salvo com ID {}", 
                    pacienteSalvo.getNome(), pacienteSalvo.getId());
    }

    public List<Paciente> listarTodosPacientes() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarPacientesAtendidos() {
        return pacienteRepository.findByStatus("FINALIZADO");
    }

    public Paciente buscarPacientePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado com ID: " + id));
    }
}
