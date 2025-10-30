package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.*;
import com.clinica.gestor_citas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}