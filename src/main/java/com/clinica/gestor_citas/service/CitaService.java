package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.*;
import com.clinica.gestor_citas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    public Cita registrarCita(Long usuarioId, Long medicoId, Long especialidadId, Long horarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setDisponible(false);
        horarioRepository.save(horario);

        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setMedico(medico);
        cita.setEspecialidad(especialidad);
        cita.setHorario(horario);

        return citaRepository.save(cita);
    }


    public Cita registrarCita(Cita cita) {
        if (cita.getUsuario() == null || cita.getMedico() == null ||
                cita.getEspecialidad() == null || cita.getHorario() == null) {
            throw new RuntimeException("Datos incompletos para registrar la cita");
        }

        // Marcar el horario como no disponible
        cita.getHorario().setDisponible(false);
        horarioRepository.save(cita.getHorario());

        return citaRepository.save(cita);
    }




    public List<Cita> listarCitasPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioIdUsuario(usuarioId);
    }


    public Cita actualizarCita(Long citaId, Long medicoId, Long especialidadId, Long horarioId) {

        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        Horario nuevoHorario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        // Liberar horario anterior
        Horario antiguoHorario = cita.getHorario();
        antiguoHorario.setDisponible(true);
        horarioRepository.save(antiguoHorario);

        // Asignar nuevo horario y bloquearlo
        nuevoHorario.setDisponible(false);
        horarioRepository.save(nuevoHorario);

        // Actualizar datos de la cita
        cita.setMedico(medico);
        cita.setEspecialidad(especialidad);
        cita.setHorario(nuevoHorario);

        return citaRepository.save(cita);
    }

    public void eliminarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Liberar horario
        Horario horario = cita.getHorario();
        horario.setDisponible(true);
        horarioRepository.save(horario);

        citaRepository.delete(cita);
    }
}