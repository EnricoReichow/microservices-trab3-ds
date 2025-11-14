package com.example.logger.service;

import com.example.logger.domain.PacienteDTO;
import com.example.logger.model.entity.PacienteEntity;
import com.example.logger.model.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Busca ou cria um paciente no banco.
     * Se o paciente já existe, atualiza os dados divergentes.
     *
     * @param pacienteDTO O DTO vindo da fila
     * @return A entidade PacienteEntity (nova ou atualizada)
     */
    @Transactional
    public PacienteEntity processarPaciente(PacienteDTO pacienteDTO) {
        // Busca pelo nome (que é único)
        Optional<PacienteEntity> optionalPaciente = pacienteRepository.findByNome(pacienteDTO.getNome());

        PacienteEntity paciente;
        if (optionalPaciente.isPresent()) {
            // 1. PACIENTE EXISTE: Atualiza dados divergentes
            paciente = optionalPaciente.get();

            // Atualiza apenas se for diferente (evita escritas desnecessárias)
            if (dtoDiverge(paciente, pacienteDTO)) {
                paciente.setSexoBiologico(pacienteDTO.getSexoBiologico());
                paciente.setDataNascimento(pacienteDTO.getDataNascimento());
                paciente.setTipoSanguineo(pacienteDTO.getTipoSanguineo());
                paciente.setEstadoCivil(pacienteDTO.getEstadoCivil());
                paciente = pacienteRepository.save(paciente);
            }
        } else {
            // 2. PACIENTE NÃO EXISTE: Cria um novo
            paciente = new PacienteEntity();
            paciente.setNome(pacienteDTO.getNome());
            paciente.setSexoBiologico(pacienteDTO.getSexoBiologico());
            paciente.setDataNascimento(pacienteDTO.getDataNascimento());
            paciente.setTipoSanguineo(pacienteDTO.getTipoSanguineo());
            paciente.setEstadoCivil(pacienteDTO.getEstadoCivil());
            paciente = pacienteRepository.save(paciente);
        }
        return paciente;
    }

    /**
     * Método auxiliar para verificar se os dados do DTO são diferentes
     * dos dados já persistidos no banco.
     */
    private boolean dtoDiverge(PacienteEntity entity, PacienteDTO dto) {
        return !safeEquals(entity.getSexoBiologico(), dto.getSexoBiologico()) ||
                !safeEquals(entity.getDataNascimento(), dto.getDataNascimento()) ||
                !safeEquals(entity.getTipoSanguineo(), dto.getTipoSanguineo()) ||
                !safeEquals(entity.getEstadoCivil(), dto.getEstadoCivil());
    }

    /**
     * Compara duas strings de forma segura (nulo-seguro).
     */
    private boolean safeEquals(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

    /**
     * Retorna todos os pacientes cadastrados (para o Controller).
     */
    @Transactional(readOnly = true)
    public List<PacienteEntity> listarTodosPacientes() {
        return pacienteRepository.findAll();
    }
}