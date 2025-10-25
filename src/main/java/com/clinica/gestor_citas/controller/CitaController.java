package com.clinica.gestor_citas.controller;

import com.clinica.gestor_citas.model.Cita;
import com.clinica.gestor_citas.service.CitaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }



    @GetMapping
    public List<Cita> listarCitas() {
        return citaService.listarCitas();
    }


    @PostMapping
    public Cita crearCita(@RequestBody Cita cita) {
        return citaService.guardarCita(cita);
    }

///si el cliente quiere cancelar su cita y
// no conoce el id de su cita y esta no posee un nombre, como michi la identifico para borrarla pipipi
    @DeleteMapping("/{id}")
    public void eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
    }
}