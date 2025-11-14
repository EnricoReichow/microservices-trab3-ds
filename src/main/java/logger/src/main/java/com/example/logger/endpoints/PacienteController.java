package com.example.logger.endpoints;

import com.example.logger.model.entity.PacienteEntity;
import com.example.logger.service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * Endpoint HTTP GET para listar todos os pacientes cadastrados
     * na tabela 'pacientes'.
     */
    @GetMapping
    public ResponseEntity<List<PacienteEntity>> listarTodosPacientes() {
        List<PacienteEntity> pacientes = pacienteService.listarTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }
}