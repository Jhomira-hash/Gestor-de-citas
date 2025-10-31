package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.Horario;
import com.clinica.gestor_citas.model.Medico;
import com.clinica.gestor_citas.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    public List<Horario> listarPorMedico(Long medicoId) {
        return horarioRepository.findByMedico_IdMedicoAndDisponibleTrue(medicoId);
    }

    public Optional<Horario> buscarHorario(Long medicoId, LocalDate fecha, LocalTime hora) {
        return horarioRepository.findByMedico_IdMedicoAndFechaAndHora(medicoId, fecha, hora);
    }
    public List<Horario> horariosPorNombreMedicoYFecha(String nombreMedico, LocalDate fecha) {
        return horarioRepository.findAll()
                .stream()
                .filter(h -> h.getMedico() != null &&
                        h.getMedico().getNombre().equalsIgnoreCase(nombreMedico) &&
                        h.getFecha().equals(fecha) &&
                        h.getDisponible())
                .toList();
    }

    public void reservarHorario(Horario horario) {
        horario.setDisponible(false);
        horarioRepository.save(horario);
    }

    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }
}