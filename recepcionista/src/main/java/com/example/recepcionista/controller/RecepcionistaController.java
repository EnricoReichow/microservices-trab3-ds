package com.example.recepcionista.controller;

import com.example.recepcionista.dto.PacienteRequest;
import com.example.recepcionista.model.Paciente;
import com.example.recepcionista.service.RecepcionistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recepcionista")
public class RecepcionistaController {

    @Autowired
    private RecepcionistaService recepcionistaService;

    /**
     * Endpoint para registrar um novo paciente
     */
    @PostMapping("/pacientes")
    public ResponseEntity<Paciente> registrarPaciente(@RequestBody PacienteRequest request) {
        Paciente paciente = new Paciente(
            request.getNome(),
            request.getSexoBiologico(),
            request.getDataNascimento(),
            request.getTipoSanguineo(),
            request.getEstadoCivil(),
            request.getSintomas()
        );
        
        Paciente pacienteRegistrado = recepcionistaService.registrarPaciente(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteRegistrado);
    }

    /**
     * Endpoint para listar todos os pacientes
     */
    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> listarPacientes() {
        List<Paciente> pacientes = recepcionistaService.listarTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para buscar paciente por ID
     */
    @GetMapping("/pacientes/{id}")
    public ResponseEntity<Paciente> buscarPaciente(@PathVariable Long id) {
        Paciente paciente = recepcionistaService.buscarPacientePorId(id);
        return ResponseEntity.ok(paciente);
    }

    /**
     * Endpoint para listar pacientes por status
     */
    @GetMapping("/pacientes/status/{status}")
    public ResponseEntity<List<Paciente>> listarPacientesPorStatus(@PathVariable String status) {
        List<Paciente> pacientes = recepcionistaService.buscarPacientesPorStatus(status);
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Recepcionista service is running");
    }
}
