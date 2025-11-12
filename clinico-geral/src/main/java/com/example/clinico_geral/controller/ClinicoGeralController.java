package com.example.clinico_geral.controller;

import com.example.clinico_geral.model.Paciente;
import com.example.clinico_geral.service.ClinicoGeralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinico-geral")
public class ClinicoGeralController {

    @Autowired
    private ClinicoGeralService clinicoGeralService;

    /**
     * Endpoint para listar todos os pacientes
     */
    @GetMapping("/pacientes")
    public ResponseEntity<List<Paciente>> listarPacientes() {
        List<Paciente> pacientes = clinicoGeralService.listarTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para listar pacientes atendidos
     */
    @GetMapping("/pacientes/atendidos")
    public ResponseEntity<List<Paciente>> listarPacientesAtendidos() {
        List<Paciente> pacientes = clinicoGeralService.listarPacientesAtendidos();
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Endpoint para buscar paciente por ID
     */
    @GetMapping("/pacientes/{id}")
    public ResponseEntity<Paciente> buscarPaciente(@PathVariable Long id) {
        Paciente paciente = clinicoGeralService.buscarPacientePorId(id);
        return ResponseEntity.ok(paciente);
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Clínico Geral service is running");
    }
}
