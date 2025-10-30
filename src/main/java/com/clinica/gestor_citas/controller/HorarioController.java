package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    public ResponseEntity<List<Horario>> listarTodos() {
        List<Horario> horarios = horarioService.listarTodos();
        if (horarios.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/medico/{id}")
    public ResponseEntity<List<Horario>> listarPorMedico(@PathVariable Long id) {
        List<Horario> lista = horarioService.listarPorMedico(id);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarHorario(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String hora) {

        try {
            String horaNormalizada = hora.trim();
            if (horaNormalizada.matches("^\\d{1,2}$")) horaNormalizada += ":00:00";
            else if (horaNormalizada.matches("^\\d{1,2}:\\d{2}$")) horaNormalizada += ":00";

            LocalTime horaParsed = LocalTime.parse(horaNormalizada);

            Optional<Horario> horarioOpt = horarioService.buscarHorario(medicoId, fecha, horaParsed);

            return horarioOpt
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body("Formato de hora inválido. Use HH:mm o HH:mm:ss (ejemplo: 10:00 o 10:00:00)");
        }
    }
}
