package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.repository.EspecialidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }
    public List<Especialidad> listarEspecialidades() {
        return especialidadRepository.findAll();
    }
    public Optional<Especialidad> buscarPorNombre(String nombre) {
        return especialidadRepository.findAll()
                .stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }
}

