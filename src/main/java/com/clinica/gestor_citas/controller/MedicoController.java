package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Especialidad;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.service.MedicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public List<Medico> listarMedicos() {
        return medicoService.listarMedicos();
    }

    @GetMapping("/especialidad/{nombre}")
    public List<Medico> buscarPorEspecialidadNombre(@PathVariable String nombre) {
        return medicoService.buscarPorEspecialidadNombre(nombre);
    }
}