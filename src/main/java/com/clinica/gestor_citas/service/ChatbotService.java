package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final GroqChatService groqChatService;
    private final EspecialidadService especialidadService;
    private final MedicoService medicoService;

    public ChatbotService(GroqChatService groqChatService, EspecialidadService especialidadService, MedicoService medicoService) {
        this.groqChatService = groqChatService;
        this.especialidadService = especialidadService;
        this.medicoService = medicoService;
    }

    public ChatResponse procesarConversacion(List<ChatMessage> historial, Usuario usuario) {

        // 1. Determinar el estado del usuario para el contexto
        String infoUsuario;
        if (usuario != null) {
            infoUsuario = "Estado Usuario: LOGUEADO. Nombre: " + usuario.getNombre() + " " + usuario.getApellido() +
                    ". (PUEDE agendar cita).";
        } else {
            infoUsuario = "Estado Usuario: NO LOGUEADO (ANÓNIMO). (NO puede agendar cita, debe iniciar sesión primero).";
        }

        // 2. Cargar el contexto dinámico de la clínica (Especialidades, Médicos y Estado Usuario)
        String contexto = cargarContextoClinica(infoUsuario);

        // 3. Enviar todo a la IA (Groq)
        Map<String, String> respuestaIA = groqChatService.generarRespuesta(historial, contexto);

        // 4. Procesar la respuesta de la IA
        ChatResponse response = new ChatResponse();
        String mensajeBot = respuestaIA.getOrDefault("mensaje", "Lo siento, hubo un error al procesar tu respuesta.");
        String accion = respuestaIA.getOrDefault("action", "NINGUNA");
        String dato = respuestaIA.getOrDefault("dato", "");

        response.setMessage(mensajeBot);
        response.setAction(accion);

        // DEBUG EN CONSOLA (Mira esto en IntelliJ)
        System.out.println("🤖 IA Acción: " + accion + " | Dato: " + dato);

        // 5. Ejecutar lógica específica según la acción dictada por la IA
        if ("FILTRAR_ESPECIALIDAD".equalsIgnoreCase(accion)) {
            Optional<Especialidad> esp = especialidadService.buscarPorNombre(dato);
            if (esp.isPresent()) {
                response.setEspecialidadId(esp.get().getIdEspecialidad());
                System.out.println("✅ Especialidad encontrada ID: " + esp.get().getIdEspecialidad());
            } else {
                System.out.println("❌ Especialidad NO encontrada: " + dato);
            }
        }
        else if ("SELECCIONAR_MEDICO".equalsIgnoreCase(accion)) {
            Optional<Medico> medico = buscarMedicoFlexible(dato);

            if (medico.isPresent()) {
                response.setPredictedSpecialty(medico.get().getIdMedico().toString());
                if (medico.get().getEspecialidad() != null) {
                    response.setEspecialidadId(medico.get().getEspecialidad().getIdEspecialidad());
                }
                System.out.println("✅ Médico encontrado: " + medico.get().getNombre() + " (ID: " + medico.get().getIdMedico() + ")");
            } else {
                System.out.println("❌ Médico NO encontrado para el dato: " + dato);
                response.setAction("NINGUNA");
            }
        }
        else if ("REDIRIGIR_CITA".equalsIgnoreCase(accion)) {
            // No se requiere lógica extra, el frontend manejará la redirección
            response.setAction("REDIRIGIR_CITA");
        }
        else if ("CONFIRMAR_CITA".equalsIgnoreCase(accion)) {
            // El frontend hará clic en el botón de reservar
            response.setAction("CONFIRMAR_CITA");
        }

        return response;
    }

    // Construye el Prompt del Sistema con toda la información actualizada de la BD
    private String cargarContextoClinica(String infoUsuario) {
        List<Especialidad> especialidades = especialidadService.listarEspecialidades();
        List<Medico> medicos = medicoService.listarMedicos();

        StringBuilder sb = new StringBuilder();

        // Información crítica del usuario
        sb.append("INFORMACIÓN DEL USUARIO ACTUAL:\n").append(infoUsuario).append("\n\n");

        // Lista de Especialidades
        sb.append("LISTA DE ESPECIALIDADES DISPONIBLES:\n[");
        sb.append(especialidades.stream()
                .map(Especialidad::getNombre)
                .collect(Collectors.joining(", ")));
        sb.append("]\n\n");

        // Lista de Médicos detallada
        sb.append("MÉDICOS DISPONIBLES:\n");
        for (Medico m : medicos) {
            String nombreEsp = (m.getEspecialidad() != null) ? m.getEspecialidad().getNombre() : "General";
            sb.append(String.format("- %s %s (%s)\n", m.getNombre(), m.getApellido(), nombreEsp));
        }
        return sb.toString();
    }

    /**
     * Busca un médico en la lista intentando coincidir nombre o apellido.
     */
    private Optional<Medico> buscarMedicoFlexible(String inputIA) {
        if (inputIA == null || inputIA.isEmpty()) return Optional.empty();

        // 1. Limpieza: Quitamos "Dr.", "Dra." y espacios extra
        String busqueda = inputIA.toLowerCase()
                .replace("dr.", "")
                .replace("dra.", "")
                .replace("doctor", "")
                .trim();

        List<Medico> todos = medicoService.listarMedicos();

        // 2. Intentamos encontrar coincidencia
        return todos.stream()
                .filter(m -> {
                    String nombreCompleto = (m.getNombre() + " " + m.getApellido()).toLowerCase();
                    String nombreSolo = m.getNombre().toLowerCase();
                    String apellidoSolo = m.getApellido().toLowerCase();

                    // Lógica:
                    // - Si el nombre completo contiene la búsqueda (Ej: "Juan Pérez" contiene "Juan")
                    // - O si la búsqueda contiene el nombre + apellido (Ej: "Dr. Juan Pérez" contiene "Juan Pérez")
                    return nombreCompleto.contains(busqueda) ||
                            busqueda.contains(nombreCompleto) ||
                            busqueda.contains(nombreSolo) ||
                            busqueda.contains(apellidoSolo);
                })
                .findFirst();
    }
}

