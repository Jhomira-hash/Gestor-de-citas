package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.service.MedicoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }


    @GetMapping
    public List<Medico> listarMedicos() {
        return medicoService.listarMedicos();
    }

    @GetMapping("/especialidad/{idEspecialidad}")
    public List<Medico> listarPorEspecialidad(@PathVariable Long idEspecialidad) {
        return medicoService.listarMedicosPorEspecialidad(idEspecialidad);
    }
}