package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping("/medico/nombre/{nombre}")
    public ResponseEntity<List<Horario>> obtenerHorariosPorNombreMedico(@PathVariable String nombre) {
        List<Horario> horarios = horarioService.horariosPorNombreMedico(nombre);
        if (horarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(horarios);
    }

    // por si el usuario puede reservar su cita en base a un hoario que este tega en mente??????
    @GetMapping("/medico/nombre/{nombre}/fecha/{fecha}")
    public ResponseEntity<List<Horario>> obtenerHorariosPorNombreMedicoYFecha(
            @PathVariable String nombre,
            @PathVariable String fecha) {

        LocalDate localDate = LocalDate.parse(fecha); // formato esperado: yyyy-MM-dd
        List<Horario> horarios = horarioService.horariosPorNombreMedicoYFecha(nombre, localDate);
        if (horarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(horarios);
    }

    @PutMapping("/reservar")
    public ResponseEntity<String> reservarHorario(@RequestBody Horario horario) {
        if (!horario.getDisponible()) {
            return ResponseEntity.badRequest().body("El horario ya está reservado");
        }
        horarioService.reservarHorario(horario);
        return ResponseEntity.ok("Horario reservado exitosamente");
    }
}