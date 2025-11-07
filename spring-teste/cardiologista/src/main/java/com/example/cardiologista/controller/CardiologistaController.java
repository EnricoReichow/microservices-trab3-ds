package com.example.cardiologista.controller;

import com.example.cardiologista.model.Paciente;
import com.example.cardiologista.service.CardiologistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cardiologista")
public class CardiologistaController {

    @Autowired
    private CardiologistaService cardiologistaService;

    /**
     * Endpoint para listar todos os pacientes
     */
    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> listarPacientes() {
        List<Paciente> pacientes = cardiologistaService.listarTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para listar pacientes atendidos
     */
    @GetMapping("/pacientes/atendidos")
    public ResponseEntity<List<Paciente>> listarPacientesAtendidos() {
        List<Paciente> pacientes = cardiologistaService.listarPacientesAtendidos();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para buscar paciente por ID
     */
    @GetMapping("/pacientes/{id}")
    public ResponseEntity<Paciente> buscarPaciente(@PathVariable Long id) {
        Paciente paciente = cardiologistaService.buscarPacientePorId(id);
        return ResponseEntity.ok(paciente);
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cardiologista service is running");
    }
}
