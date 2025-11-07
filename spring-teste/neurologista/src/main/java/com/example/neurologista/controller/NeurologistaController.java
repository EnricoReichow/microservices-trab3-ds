package com.example.neurologista.controller;

import com.hospital.common.model.Paciente;
import com.hospital.neurologista.service.NeurologistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/neurologista")
public class NeurologistaController {

    @Autowired
    private NeurologistaService neurologistaService;

    /**
     * Endpoint para listar todos os pacientes
     */
    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> listarPacientes() {
        List<Paciente> pacientes = neurologistaService.listarTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para listar pacientes atendidos
     */
    @GetMapping("/pacientes/atendidos")
    public ResponseEntity<List<Paciente>> listarPacientesAtendidos() {
        List<Paciente> pacientes = neurologistaService.listarPacientesAtendidos();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para buscar paciente por ID
     */
    @GetMapping("/pacientes/{id}")
    public ResponseEntity<Paciente> buscarPaciente(@PathVariable Long id) {
        Paciente paciente = neurologistaService.buscarPacientePorId(id);
        return ResponseEntity.ok(paciente);
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Neurologista service is running");
    }
}
