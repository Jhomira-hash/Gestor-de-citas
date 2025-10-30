package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Cita;
import com.clinica.gestor_citas.model.Usuario;
import com.clinica.gestor_citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<?> registrarCita(@RequestBody Map<String, Long> datos, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        Long medicoId = datos.get("medicoId");
        Long especialidadId = datos.get("especialidadId");
        Long horarioId = datos.get("horarioId");

        Cita cita = citaService.registrarCita(usuario.getIdUsuario(), medicoId, especialidadId, horarioId);
        return ResponseEntity.ok(cita);
    }
}
