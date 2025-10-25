package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.service.EspecialidadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<Especialidad> listarEspecialidades() {
        return especialidadService.listarEspecialidades();
    }

    @GetMapping("/{nombre}")
    public Optional<Especialidad> buscarPorNombre(@PathVariable String nombre) {
        return especialidadService.buscarPorNombre(nombre);
    }
}
