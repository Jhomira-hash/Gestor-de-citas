package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.ChatMessage;
import com.clinica.gestor_citas.model.CitaExtraida;
import com.clinica.gestor_citas.model.Especialidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GroqChatService {
    private static final Logger logger = LoggerFactory.getLogger(GroqChatService.class);

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    private final RestTemplate restTemplate;
    private final EspecialidadService especialidadService;
    private final MedicoService medicoService;

    // Almacenamiento temporal de conversaciones
    private final Map<String, List<ChatMessage>> conversationStore = new ConcurrentHashMap<>();

    public GroqChatService(RestTemplate restTemplate,
                           EspecialidadService especialidadService,
                           MedicoService medicoService) {
        this.restTemplate = restTemplate;
        this.especialidadService = especialidadService;
        this.medicoService = medicoService;
    }

    public String processMessage(String conversationId, String userMessage) {
        try {
            logger.info("Procesando mensaje para conversación: {}", conversationId);

            // Obtener o crear historial
            List<ChatMessage> history = conversationStore.computeIfAbsent(
                    conversationId,
                    k -> new ArrayList<>()
            );

            // Agregar mensaje del usuario
            history.add(new ChatMessage("user", userMessage));

            // Construir contexto con especialidades disponibles
            String systemPrompt = buildSystemPrompt();

            // Llamar a Groq
            String aiResponse = callGroqAPI(systemPrompt, history);

            // Agregar respuesta del asistente
            history.add(new ChatMessage("assistant", aiResponse));

            logger.info("Mensaje procesado exitosamente");
            return aiResponse;

        } catch (Exception e) {
            logger.error("Error procesando mensaje: {}", e.getMessage(), e);
            return "Lo siento, tengo problemas para procesar tu solicitud. Por favor, intenta nuevamente.";
        }
    }

    private String callGroqAPI(String systemPrompt, List<ChatMessage> history) {
        try {
            logger.debug("Llamando a Groq API: {}", groqApiUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            // Construir mensajes
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }

            Map<String, Object> requestBody = Map.of(
                    "model", "openai/gpt-oss-20b",
                    "messages", messages,
                    "temperature", 0.7,
                    "max_tokens", 1024
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.debug("Enviando petición a Groq...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    groqApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            logger.debug("Respuesta recibida con status: {}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    logger.debug("Contenido de respuesta: {}", content.substring(0, Math.min(100, content.length())));
                    return content;
                }
            }

            logger.error("Respuesta de Groq sin contenido válido");
            return "Lo siento, hubo un error al procesar tu mensaje.";

        } catch (Exception e) {
            logger.error("Error al llamar a Groq API: {}", e.getMessage(), e);
            return "Error al comunicarme con el servicio. Por favor, intenta nuevamente.";
        }
    }

    private String buildSystemPrompt() {
        try {
            // Obtener especialidades disponibles
            List<Especialidad> especialidades = especialidadService.listarEspecialidades();
            StringBuilder especialidadesStr = new StringBuilder();
            for (Especialidad esp : especialidades) {
                especialidadesStr.append("- ").append(esp.getNombre())
                        .append(": ").append(esp.getDescripcion()).append("\n");
            }

            return String.format("""
                Eres un asistente médico virtual de una clínica. Tu objetivo es:
                1. Ayudar a los pacientes a identificar la especialidad médica adecuada según sus síntomas
                2. Gestionar la reserva de citas médicas de forma conversacional
                
                ESPECIALIDADES DISPONIBLES:
                %s
                
                PROCESO DE ATENCIÓN:
                1. Saluda cordialmente y pregunta en qué puedes ayudar
                2. Si el paciente describe síntomas, analízalos y recomienda 1-2 especialidades apropiadas
                3. Pregunta si desea agendar una cita con alguna especialidad
                4. Si acepta, solicita:
                   - Especialidad deseada (si no la mencionó)
                   - Fecha preferida (formato: dd/MM/yyyy o día de la semana)
                   - Hora aproximada (mañana, tarde, o hora específica)
                5. Confirma todos los datos antes de finalizar
                
                IMPORTANTE:
                - Sé empático, cordial y profesional
                - NO diagnostiques enfermedades
                - Si menciona síntomas graves (dolor en el pecho, dificultad para respirar, sangrado severo), 
                  recomienda ir a EMERGENCIAS inmediatamente
                - Mantén respuestas breves (máximo 3-4 líneas)
                - Usa un tono conversacional, no formal en exceso
                
                FORMATO DE CONFIRMACIÓN:
                Cuando tengas todos los datos necesarios, responde incluyendo:
                "DATOS_COMPLETOS: [especialidad] | [fecha en dd/MM/yyyy] | [hora en HH:mm] | [síntomas resumidos]"
                
                Ejemplo: "DATOS_COMPLETOS: Cardiología | 05/11/2025 | 10:00 | dolor en pecho, fatiga"
                
                Si el usuario pregunta por médicos específicos o disponibilidad, indica que verificarás 
                los horarios disponibles.
                """, especialidadesStr.toString());
        } catch (Exception e) {
            logger.error("Error construyendo prompt del sistema", e);
            return "Eres un asistente médico virtual. Ayuda a los pacientes a agendar citas.";
        }
    }

    public CitaExtraida extraerDatosCita(String conversationId) {
        List<ChatMessage> history = conversationStore.get(conversationId);
        if (history == null) return null;

        // Buscar el último mensaje con DATOS_COMPLETOS
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage msg = history.get(i);
            if (msg.getRole().equals("assistant") &&
                    msg.getContent().contains("DATOS_COMPLETOS:")) {
                return parsearDatosCita(msg.getContent());
            }
        }
        return null;
    }

    private CitaExtraida parsearDatosCita(String content) {
        try {
            // Buscar el patrón DATOS_COMPLETOS: xxx | xxx | xxx | xxx
            Pattern pattern = Pattern.compile(
                    "DATOS_COMPLETOS:\\s*([^|]+)\\|([^|]+)\\|([^|]+)\\|(.+?)(?:\\n|$)"
            );
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                CitaExtraida cita = new CitaExtraida();

                String especialidad = matcher.group(1).trim();
                String fechaStr = matcher.group(2).trim();
                String horaStr = matcher.group(3).trim();
                String sintomas = matcher.group(4).trim();

                cita.setEspecialidad(especialidad);
                cita.setSintomas(sintomas);

                // Parsear fecha (dd/MM/yyyy)
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                cita.setFecha(LocalDate.parse(fechaStr, dateFormatter));

                // Parsear hora (HH:mm)
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                cita.setHora(LocalTime.parse(horaStr, timeFormatter));

                cita.setDatosCompletos(true);

                logger.info("Datos de cita extraídos: {}", cita);
                return cita;
            }
        } catch (Exception e) {
            logger.error("Error parseando datos de cita", e);
        }
        return null;
    }

    public List<String> extraerEspecialidadesMencionadas(String mensaje) {
        List<String> especialidades = new ArrayList<>();
        try {
            List<Especialidad> todasEspecialidades = especialidadService.listarEspecialidades();

            String mensajeLower = mensaje.toLowerCase();
            for (Especialidad esp : todasEspecialidades) {
                if (mensajeLower.contains(esp.getNombre().toLowerCase())) {
                    especialidades.add(esp.getNombre());
                }
            }
        } catch (Exception e) {
            logger.error("Error extrayendo especialidades", e);
        }

        return especialidades;
    }

    public void limpiarConversacion(String conversationId) {
        conversationStore.remove(conversationId);
        logger.info("Conversación limpiada: {}", conversationId);
    }

    public List<ChatMessage> obtenerHistorial(String conversationId) {
        return conversationStore.getOrDefault(conversationId, new ArrayList<>());
    }

}
