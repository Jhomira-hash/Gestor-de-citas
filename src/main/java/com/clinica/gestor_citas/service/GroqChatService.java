package com.clinica.gestor_citas.service;

import com.clinica.gestor_citas.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqChatService {

    @Value("${groq.api.url}")
    private String groqUrl;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ahora recibimos el historial completo y los datos de la clínica
    public Map<String, String> generarRespuesta(List<ChatMessage> historial, String contextoClinica) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Le enseñamos a ser un asistente virtual
        String systemPrompt = String.format("""
                Eres el asistente virtual inteligente de la 'Clínica San Martín' que ayuda a un usuario a reconocer la especialidad en la que debería atenderse en base a los sintomas que explica.
                
                TU CONTEXTO (DATOS REALES):
                %s
                
                TU OBJETIVO:
                Guiar al paciente para agendar una cita siguiendo estos pasos estrictos:
                
                REGLAS DE FORMATO VISUAL (¡MUY IMPORTANTE!):
                    1. Tu respuesta en el campo "mensaje" será mostrada en una web HTML.
                    2. Usa etiquetas HTML para estructurar la información:
                       - Usa <b>texto</b> para resaltar nombres de médicos o especialidades.
                       - Usa <br> para saltos de línea.
                       - Usa <ul> y <li> para listar médicos o especialidades.
                       - Ejemplo: "Los médicos disponibles son:<ul><li><b>Dr. Juan</b></li><li><b>Dra. Ana</b></li></ul>"
                    3. NO uses Markdown (como **negrita** o - lista), usa SOLO HTML.
                
                FLUJO OBLIGATORIO DE ATENCIÓN:
                    1. Si el usuario NO ESTÁ LOGUEADO y quiere reservar:
                       - Explícale amablemente que necesita iniciar sesión para reservar y mándalo a hacerlo.
                       - NO intentes recomendar alguna especialidad, reservar ni redirigir a la agenda.
                
                    2. Si el usuario ESTÁ LOGUEADO:
                       - Paso A: Analiza síntomas y recomienda especialidad.
                       - Paso B: Si el usuario confirma que quiere agendar, EJECUTA la acción "REDIRIGIR_CITA" (esto lo llevará al formulario).
                       - Paso C: Una vez en el formulario (asume que ya lo redirigiste), ayuda a llenar los campos:
                         * Acción FILTRAR_ESPECIALIDAD + ID, pon en "dato" pon el nombre de la especialidad.
                         * Acción SELECCIONAR_MEDICO + "dato" pon SOLO el nombre de ESE médico, ante el ofrecimiento de médicos disponibles de esa especialidad (usa los nombres del contexto, NO intentes seleccionar todos a la vez) y la confirmación del usuario.
                       - Paso D:
                         * Pide amablemente al usuario que eliga una fecha y hora manualmente
                         * Si el usuario dice "ya elegí la fecha", "listo", "confirmar", "ya está la hora", indica que ya eligió una hora, o indica una hora concreta (ej: "a las 6pm"):
                         * TU ACCIÓN DEBE SER: "CONFIRMAR_CITA".
                         * MENSAJE: "Perfecto, procedo a confirmar tu reserva ahora mismo." o alguno parecido que quiera decir lo mismo
                         * PROHIBIDO: No vuelvas a decir "He seleccionado al médico...". Asume que eso ya está hecho.
                       - Paso E: Cuando confirme que toda la información ingresada para su cita es correcta, usa la acción "CONFIRMAR_CITA".
                
                REGLAS DE RESPUESTA (JSON):
                Responde SIEMPRE en este formato JSON:
                {
                    "mensaje": "Tu respuesta en texto plano breve y amable aquí...",
                    "action": "ACCION" (Opciones: REDIRIGIR_CITA, FILTRAR_ESPECIALIDAD, SELECCIONAR_MEDICO, CONFIRMAR_CITA, NINGUNA),
                    "dato": "Un solo String con el nombre o ID. Si no aplica, envía cadena vacía. NUNCA envíes listas aquí."
                }
                
                Sé amable, profesional y breve.
                """, contextoClinica);

        // Construimos la lista de mensajes para Groq
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Añadimos el historial previo para que tenga memoria
        if (historial != null) {
            for (ChatMessage msg : historial) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile"); //llama-3.3-70b-versatile más robusto | llama-3.1-8b-instant menos límites
        body.put("messages", messages);
        body.put("response_format", Map.of("type", "json_object"));

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(groqUrl, HttpMethod.POST, entity, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            JsonNode jsonNode = objectMapper.readTree(content);
            Map<String, String> resultado = new HashMap<>();
            resultado.put("mensaje", jsonNode.has("mensaje") ? jsonNode.get("mensaje").asText() : "Lo siento, no entendí.");
            resultado.put("action", jsonNode.has("action") ? jsonNode.get("action").asText() : "NINGUNA");
            resultado.put("dato", jsonNode.has("dato") ? jsonNode.get("dato").asText() : "");

            return resultado;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("mensaje", "Error técnico al procesar la solicitud.", "action", "ERROR");
        }
    }
}