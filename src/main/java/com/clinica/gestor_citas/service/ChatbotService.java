package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.*;
import com.clinica.gestor_citas.repository.UsuarioRepository; // AGREGAR ESTE IMPORT
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatbotService {

    private final GroqChatService groqChatService;
    private final CitaService citaService;
    private final MedicoService medicoService;
    private final HorarioService horarioService;
    private final EspecialidadService especialidadService;
    private final UsuarioRepository usuarioRepository; // Ahora debería reconocerlo

    public ChatbotService(GroqChatService groqChatService,
                          CitaService citaService,
                          MedicoService medicoService,
                          HorarioService horarioService,
                          EspecialidadService especialidadService,
                          UsuarioRepository usuarioRepository) {
        this.groqChatService = groqChatService;
        this.citaService = citaService;
        this.medicoService = medicoService;
        this.horarioService = horarioService;
        this.especialidadService = especialidadService;
        this.usuarioRepository = usuarioRepository;
    }

    public ChatResponse procesarMensaje(ChatRequest request) {
        ChatResponse response = new ChatResponse();

        // Procesar mensaje con Groq
        String aiMessage = groqChatService.processMessage(
                request.getConversationId(),
                request.getMessage()
        );

        response.setMessage(aiMessage);
        response.setConversationId(request.getConversationId());

        // Extraer especialidades mencionadas
        List<String> especialidades = groqChatService.extraerEspecialidadesMencionadas(aiMessage);
        response.setEspecialidadesSugeridas(especialidades);

        // Si se mencionó una especialidad, sugerir médicos
        if (!especialidades.isEmpty()) {
            List<Medico> medicos = medicoService.listarMedicosPorEspecialidad(especialidades.get(0));
            response.setMedicosSugeridos(medicos);
        }

        // Verificar si hay datos completos para crear cita
        CitaExtraida citaExtraida = groqChatService.extraerDatosCita(request.getConversationId());
        if (citaExtraida != null && citaExtraida.isDatosCompletos()) {
            response.setCitaExtraida(citaExtraida);

            // Intentar crear la cita automáticamente
            if (request.getUsuarioId() != null) {
                try {
                    Cita citaCreada = crearCitaAutomatica(citaExtraida, request.getUsuarioId());
                    response.setCitaCreada(citaCreada);
                    response.setMessage(response.getMessage() +
                            "\n\n✅ ¡Cita confirmada exitosamente! Tu número de cita es: " +
                            citaCreada.getIdCita());
                } catch (Exception e) {
                    response.setMessage(response.getMessage() +
                            "\n\n⚠️ " + e.getMessage());
                }
            }
        }

        return response;
    }

    private Cita crearCitaAutomatica(CitaExtraida citaExtraida, Long usuarioId) {
        // Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Buscar especialidad
        Optional<Especialidad> especialidadOpt = especialidadService
                .buscarPorNombre(citaExtraida.getEspecialidad());
        if (especialidadOpt.isEmpty()) {
            throw new RuntimeException("Especialidad no encontrada");
        }

        // Buscar médicos de esa especialidad
        List<Medico> medicos = medicoService
                .buscarPorEspecialidadNombre(citaExtraida.getEspecialidad());
        if (medicos.isEmpty()) {
            throw new RuntimeException("No hay médicos disponibles para esta especialidad");
        }

        // Buscar horario disponible
        Medico medico = medicos.get(0); // Por ahora tomamos el primero
        List<Horario> horariosDisponibles = horarioService
                .horariosPorNombreMedicoYFecha(
                        medico.getNombre(),
                        citaExtraida.getFecha()
                );

        // Buscar el horario más cercano a la hora solicitada
        Horario horarioSeleccionado = encontrarHorarioCercano(
                horariosDisponibles,
                citaExtraida.getHora()
        );

        if (horarioSeleccionado == null) {
            throw new RuntimeException(
                    "No hay horarios disponibles para la fecha solicitada. " +
                            "Por favor, elige otra fecha."
            );
        }

        // Crear la cita
        Cita cita = new Cita();
        cita.setUsuario(usuarioOpt.get());
        cita.setMedico(medico);
        cita.setEspecialidad(especialidadOpt.get());
        cita.setHorario(horarioSeleccionado);

        // Reservar el horario
        horarioService.reservarHorario(horarioSeleccionado);

        // Guardar la cita
        return citaService.registrarCita(cita);
    }

    private Horario encontrarHorarioCercano(List<Horario> horarios, LocalTime horaDeseada) {
        if (horarios.isEmpty()) return null;

        Horario mejorOpcion = horarios.get(0);
        long menorDiferencia = Math.abs(
                mejorOpcion.getHora().toSecondOfDay() - horaDeseada.toSecondOfDay()
        );

        for (Horario horario : horarios) {
            long diferencia = Math.abs(
                    horario.getHora().toSecondOfDay() - horaDeseada.toSecondOfDay()
            );
            if (diferencia < menorDiferencia) {
                menorDiferencia = diferencia;
                mejorOpcion = horario;
            }
        }

        return mejorOpcion;
    }


}
