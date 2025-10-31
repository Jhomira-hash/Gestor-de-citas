package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }

    public List<Medico> listarMedicosPorEspecialidad(Long idEspecialidad) {
        return medicoRepository.findByEspecialidad_IdEspecialidad(idEspecialidad);
    }

    public List<Medico> listarMedicosPorNombreEspecialidad(String nombreEspecialidad) {
        return medicoRepository.findAll()
                .stream()
                .filter(m -> m.getEspecialidad() != null &&
                        m.getEspecialidad().getNombre().equalsIgnoreCase(nombreEspecialidad))
                .toList();
    }

}

