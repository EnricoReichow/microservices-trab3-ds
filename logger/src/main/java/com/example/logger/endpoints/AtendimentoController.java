package com.example.logger.endpoints;

import com.example.logger.domain.AtendimentoDTO;
import com.example.logger.model.entity.AtendimentoEntity;
import com.example.logger.service.AtendimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
    @RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    /**
     * Endpoint HTTP GET para listar todos os atendimentos cadastrados
     * na tabela 'atendimentos'.
     */
    @GetMapping
    public ResponseEntity<List<AtendimentoDTO>> listarTodosAtendimentos() {
        List<AtendimentoEntity> atendimentos = atendimentoService.listarTodosAtendimentos();

        List<AtendimentoDTO> dtos = atendimentos.stream()
                .map(a -> {
                    AtendimentoDTO dto = new AtendimentoDTO();
                    dto.setId(a.getId());
                    dto.setPacienteId(a.getPaciente().getId());
                    dto.setPacienteNome(a.getPaciente().getNome());
                    dto.setSintomas(a.getSintomas());
                    dto.setMedicoResponsavel(a.getMedicoResponsavel());
                    dto.setDataAtendimento(a.getDataAtendimento());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

