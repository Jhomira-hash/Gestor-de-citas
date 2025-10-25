package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.repository.HorarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    public List<Horario> horariosPorNombreMedico(String nombre) {
        return horarioRepository.findByMedicoNombreAndDisponibleTrue(nombre);
    }

    public List<Horario> horariosPorNombreMedicoYFecha(String nombre, LocalDate fecha) {
        return horarioRepository.findByMedicoNombreAndFechaAndDisponibleTrue(nombre, fecha);
    }

    public void reservarHorario(Horario horario) {
        horario.setDisponible(false);
        horarioRepository.save(horario);
    }
}
