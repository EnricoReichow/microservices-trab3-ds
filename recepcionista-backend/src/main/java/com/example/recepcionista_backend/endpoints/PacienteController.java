package com.example.recepcionista_backend.endpoints;

import com.example.recepcionista_backend.model.Paciente;
import com.example.recepcionista_backend.service.RabbitMQService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes") // Define o caminho base para este controller
public class PacienteController {

    private final RabbitMQService rabbitMQService;

    // Injeta o serviço do RabbitMQ
    public PacienteController(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    /**
     * Endpoint HTTP POST para registrar um novo paciente.
     * Ele espera receber um JSON no corpo da requisição que
     * corresponda à estrutura da classe Paciente.
     *
     * @param paciente O objeto Paciente deserializado do JSON da requisição
     * @return Uma resposta HTTP (202 Aceito)
     */
    @PostMapping
    public ResponseEntity<String> registrarPaciente(@RequestBody Paciente paciente) {
        // Envia o paciente recebido via HTTP para o serviço do RabbitMQ
        rabbitMQService.sendPaciente(paciente);

        // Retorna uma resposta HTTP 202 (Accepted)
        return ResponseEntity.accepted().body("Paciente " + paciente.getNome() + " recebido e enviado para processamento.");
    }
}